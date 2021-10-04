package com.redhat.qute.services.completions;

import static com.redhat.qute.services.QuteCompletions.EMPTY_COMPLETION;
import static com.redhat.qute.services.QuteCompletions.EMPTY_FUTURE_COMPLETION;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.JavaClassMemberInfo;
import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionMetadata;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.LoopSection;
import com.redhat.qute.services.JavaDataModelCache;
import com.redhat.qute.services.QuteCompletions;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.StringUtils;

public class QuteCompletionsForExpression {

	private final JavaDataModelCache javaCache;

	public QuteCompletionsForExpression(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	public CompletableFuture<CompletionList> doCompleteExpression(Expression expression, Node nodeExpression,
			Template template, int offset, CancelChecker cancelChecker) {
		if (nodeExpression == null) {
			// ex : { | }
			return doCompleteExpressionForObjectPart(expression, null, offset, template);
		}

		if (nodeExpression.getKind() == NodeKind.ExpressionPart) {
			Part part = (Part) nodeExpression;
			switch (part.getPartKind()) {
			case Object:
				// ex : { ite|m }
				return doCompleteExpressionForObjectPart(expression, part, offset, template);
			case Property:
			case Method:
				// ex : { item.n| }
				// ex : { item.n|ame }
				Parts parts = part.getParent();
				return doCompleteExpressionForMemberPart(part, parts, template);
			default:
				break;
			}
			return QuteCompletions.EMPTY_FUTURE_COMPLETION;
		}

		if (nodeExpression.getKind() == NodeKind.ExpressionParts) {
			char previous = template.getText().charAt(offset - 1);
			switch (previous) {
			case ':': {
				// ex : { data:| }
				// ex : { data:|name }
				Parts parts = (Parts) nodeExpression;
				Part part = parts.getPartAt(offset + 1);
				return doCompleteExpressionForObjectPart(expression, part, offset, template);
			}
			case '.': {
				// ex : { item.| }
				// ex : { item.|name }
				Parts parts = (Parts) nodeExpression;
				Part part = parts.getPartAt(offset + 1);
				return doCompleteExpressionForMemberPart(part, parts, template);
			}
			}
		}
		return EMPTY_FUTURE_COMPLETION;
	}

	private CompletableFuture<CompletionList> doCompleteExpressionForMemberPart(Part part, Parts parts,
			Template template) {
		int start = part != null ? part.getStart() : parts.getEnd();
		int end = part != null ? part.getEnd() : parts.getEnd();
		String projectUri = template.getProjectUri();
		int partIndex = parts.getPreviousPartIndex(part);
		return javaCache.resolveJavaType(parts, partIndex, projectUri) //
				.thenCompose(resolvedClass -> {
					if (resolvedClass == null) {
						return EMPTY_FUTURE_COMPLETION;
					}
					if (resolvedClass.isIterable()) {
						// Completion for member of the iterable element of the given Java class
						// iterable
						// ex : completion for 'org.acme.Item' iterable element of the
						// 'java.util.List<org.acme.Item>' Java class iterable
						return javaCache.resolveJavaType(resolvedClass.getIterableType(), projectUri) //
								.thenApply(resolvedIterableClass -> {
									if (resolvedIterableClass == null) {
										return EMPTY_COMPLETION;
									}
									return doCompleteForJavaClassMembers(start, end, template, resolvedIterableClass);
								});
					}
					// Completion for member of the given Java class
					// ex : org.acme.Item
					CompletionList list = doCompleteForJavaClassMembers(start, end, template, resolvedClass);
					return CompletableFuture.completedFuture(list);

				});

	}

	private CompletionList doCompleteForJavaClassMembers(int start, int end, Template template,
			ResolvedJavaClassInfo resolvedClass) {
		CompletionList list = new CompletionList();
		list.setItems(new ArrayList<>());
		Range range = QutePositionUtility.createRange(start, end, template);
		for (JavaClassMemberInfo member : resolvedClass.getMembers()) {
			String fullClassName = member.getField();
			if (fullClassName == null) {
				fullClassName = member.getMethod();
			}
			CompletionItem item = new CompletionItem();
			item.setLabel(fullClassName);
			TextEdit textEdit = new TextEdit();
			textEdit.setRange(range);
			textEdit.setNewText(fullClassName);
			item.setTextEdit(Either.forLeft(textEdit));
			list.getItems().add(item);
		}
		return list;
	}

	private CompletableFuture<CompletionList> doCompleteExpressionForObjectPart(Expression expression, Node part,
			int offset, Template template) {
		// Completion for root object
		int partStart = part != null ? part.getStart() : offset;
		int partEnd = part != null ? part.getEnd() : offset;
		Range range = QutePositionUtility.createRange(partStart, partEnd, template);
		CompletionList list = new CompletionList();

		// Collect alias declared from parameter declaration
		doCompleteExpressionForObjectPartWithParameterAlias(template, range, list);
		// Collect declared model inside section, let, etc
		doCompleteExpressionForObjectPartWithParentNodes(part, expression, range, list);

		return CompletableFuture.completedFuture(list);
	}

	private void doCompleteExpressionForObjectPartWithParentNodes(Node part, Node node, Range range,
			CompletionList list) {
		Node parent = node != null ? node.getParent() : null;
		if (parent == null || parent.getKind() == NodeKind.Template) {
			return;
		}
		if (parent.getKind() == NodeKind.Section) {
			// Completion for metadata
			Section section = (Section) parent;
			List<SectionMetadata> metadatas = section.getMetadata();
			for (SectionMetadata metadata : metadatas) {
				String name = metadata.getName();
				CompletionItem item = new CompletionItem();
				item.setLabel(name);
				item.setKind(CompletionItemKind.Keyword);
				item.setSortText("Z" + name);
				TextEdit textEdit = new TextEdit(range, name);
				item.setTextEdit(Either.forLeft(textEdit));
				list.getItems().add(item);
			}
			if (section.isIterable()) {
				// Completion for iterable section like #each, #for
				String alias = ((LoopSection) section).getAlias();
				if (!StringUtils.isEmpty(alias)) {
					CompletionItem item = new CompletionItem();
					item.setLabel(alias);
					item.setKind(CompletionItemKind.Reference);
					TextEdit textEdit = new TextEdit(range, alias);
					item.setTextEdit(Either.forLeft(textEdit));
					list.getItems().add(item);
				}

			}
		}
		doCompleteExpressionForObjectPartWithParentNodes(part, parent, range, list);
	}

	private void doCompleteExpressionForObjectPartWithParameterAlias(Template template, Range range,
			CompletionList list) {
		List<String> aliases = template.getChildren().stream() //
				.filter(n -> n.getKind() == NodeKind.ParameterDeclaration) //
				.map(n -> ((ParameterDeclaration) n).getAlias()) //
				.filter(alias -> alias != null) //
				.collect(Collectors.toList());
		for (String alias : aliases) {
			CompletionItem item = new CompletionItem();
			item.setLabel(alias);
			item.setKind(CompletionItemKind.Reference);
			TextEdit textEdit = new TextEdit(range, alias);
			item.setTextEdit(Either.forLeft(textEdit));
			list.getItems().add(item);
		}
	}

}
