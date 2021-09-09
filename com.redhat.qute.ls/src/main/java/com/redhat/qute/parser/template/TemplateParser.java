package com.redhat.qute.parser.template;

import java.util.Objects;

import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.scanner.Scanner;
import com.redhat.qute.parser.template.scanner.ScannerState;
import com.redhat.qute.parser.template.scanner.TemplateScanner;
import com.redhat.qute.parser.template.scanner.TokenType;

/**
 * The Qute template parser.
 * 
 * @author Angelo ZERR
 *
 */
public class TemplateParser {

	private static CancelChecker DEFAULT_CANCEL_CHECKER = () -> {
	};

	public static Template parse(String content, String uri) {
		return parse(content, uri, DEFAULT_CANCEL_CHECKER);
	}

	public static Template parse(String text, String uri, CancelChecker cancelChecker) {
		return parse(new TextDocument(text, uri), cancelChecker);
	}

	public static Template parse(TextDocument textDocument, CancelChecker cancelChecker) {
		if (cancelChecker == null) {
			cancelChecker = DEFAULT_CANCEL_CHECKER;
		}
		Template template = new Template(textDocument);
		Node curr = template;

		String content = textDocument.getText();
		int endTagOpenOffset = -1;
		Scanner<TokenType, ScannerState> scanner = TemplateScanner.createScanner(content);
		TokenType token = scanner.scan();
		while (token != TokenType.EOS) {
			cancelChecker.checkCanceled();

			switch (token) {

			case StartTagOpen: {
				if (!curr.isClosed() && curr.getParent() != null) {
					// The next node's parent (curr) is not closed at this point
					// so the node's parent (curr) will have its end position updated
					// to a newer end position.
					curr.setEnd(scanner.getTokenOffset());
				}
				if (curr.isClosed() && curr.getKind() != NodeKind.Template) {
					// The next node being considered is a child of 'curr'
					// and if 'curr' is already closed then 'curr' was not updated properly.
					curr = curr.getParent();
				}
				Section child = new Section(scanner.getTokenOffset(), scanner.getTokenEnd());
				child.setStartTagOpenOffset(scanner.getTokenOffset());
				curr.addChild(child);
				curr = child;
				break;
			}

			case StartTag: {
				Section element = (Section) curr;
				element.setTag(scanner.getTokenText());
				curr.setEnd(scanner.getTokenEnd());
				break;
			}

			case StartTagClose:
				if (curr.getKind() == NodeKind.Section) {
					Section element = (Section) curr;
					curr.setEnd(scanner.getTokenEnd()); // might be later set to end tag position
					element.setStartTagCloseOffset(scanner.getTokenOffset());

					// never enters isEmptyElement() is always false
					if (element.getTag() != null && isEmptyElement(element.getTag()) && curr.getParent() != null) {
						curr.setClosed(true);
						curr = curr.getParent();
					}
				}
				curr.setEnd(scanner.getTokenEnd());
				break;

			case EndTagOpen:
				endTagOpenOffset = scanner.getTokenOffset();
				curr.setEnd(scanner.getTokenOffset());
				break;

			case EndTag:
				// end tag (ex: {/if}>)
				String closeTag = scanner.getTokenText();
				Node current = curr;

				/**
				 * eg: <a><b><c></d> will set a,b,c end position to the start of |</d>
				 */
				while (!(curr.getKind() == NodeKind.Section && Objects.equals(((Section) curr).getTag(), closeTag))
						&& curr.getParent() != null) {
					curr.setEnd(endTagOpenOffset);
					curr = curr.getParent();
				}
				if (curr != template) {
					curr.setClosed(true);
					if (curr.getKind() == NodeKind.Section) {
						((Section) curr).setEndTagOpenOffset(endTagOpenOffset);
					}
					curr.setEnd(scanner.getTokenEnd());
				} else {
					// element open tag not found (ex: <root>) add a fake element which only has an
					// end tag (no start tag).
					Section element = new Section(scanner.getTokenOffset() - 2, scanner.getTokenEnd());
					element.setEndTagOpenOffset(endTagOpenOffset);
					element.setTag(closeTag);
					current.addChild(element);
					curr = element;
				}
				break;

			case StartTagSelfClose:
				if (curr.getParent() != null) {
					curr.setClosed(true);
					((Section) curr).setSelfClosed(true);
					curr.setEnd(scanner.getTokenEnd());
					curr = curr.getParent();
				}
				break;

			case EndTagClose:
				if (curr.getParent() != null) {
					curr.setEnd(scanner.getTokenEnd());
					((Section) curr).setEndTagCloseOffset(scanner.getTokenOffset());
					curr = curr.getParent();
				}
				break;

			case StartExpression: {
				// In case the tag before the expression (curr) was not properly closed
				// curr should be set to the root node.
				if (curr.isClosed() && curr.getKind() != NodeKind.Template) {
					curr = curr.getParent();
				}
				int start = scanner.getTokenOffset();
				int end = scanner.getTokenEnd();
				Expression expression = new Expression(start, end);
				curr.addChild(expression);
				curr = expression;
				break;
			}

			case EndExpression: {
				int end = scanner.getTokenEnd();
				Expression expression = (Expression) curr;
				expression.setClosed(true);
				expression.setEnd(end);
				curr = curr.getParent();
				break;
			}

			case StartComment: {
				// In case the tag before the expression (curr) was not properly closed
				// curr should be set to the root node.
				if (curr.isClosed() && curr.getKind() != NodeKind.Template) {
					curr = curr.getParent();
				}
				int start = scanner.getTokenOffset();
				int end = scanner.getTokenEnd();
				Comment comment = new Comment(start, end);
				curr.addChild(comment);
				curr = comment;
				break;
			}

			case Comment: {
				Comment comment = (Comment) curr;
				comment.setStartContent(scanner.getTokenOffset());
				comment.setEndContent(scanner.getTokenEnd());
				break;
			}

			case EndComment: {
				int end = scanner.getTokenEnd();
				Comment comment = (Comment) curr;
				comment.setClosed(true);
				comment.setEnd(end);
				curr = curr.getParent();
				break;
			}

			case StartParameterDeclaration: {
				// In case the tag before the expression (curr) was not properly closed
				// curr should be set to the root node.
				if (curr.isClosed() && curr.getKind() != NodeKind.Template) {
					curr = curr.getParent();
				}
				int start = scanner.getTokenOffset();
				int end = scanner.getTokenEnd();
				ParameterDeclaration parameter = new ParameterDeclaration(start, end);
				curr.addChild(parameter);
				curr = parameter;
				break;
			}

			case ParameterDeclaration: {
				ParameterDeclaration parameter = (ParameterDeclaration) curr;
				parameter.setStartContent(scanner.getTokenOffset());
				parameter.setEndContent(scanner.getTokenEnd());
				break;
			}

			case EndParameterDeclaration: {
				int end = scanner.getTokenEnd();
				ParameterDeclaration parameter = (ParameterDeclaration) curr;
				parameter.setClosed(true);
				parameter.setEnd(end);
				curr = curr.getParent();
				break;
			}

			case Content: {
				int start = scanner.getTokenOffset();
				int end = scanner.getTokenEnd();
				Text text = new Text(start, end);
				curr.addChild(text);
				break;
			}
			default:
			}
			token = scanner.scan();
		}
		while (curr.getParent() != null) {
			curr.setEnd(content.length());
			curr = curr.getParent();
		}
		return template;
	}

	private static boolean isEmptyElement(String tag) {
		return false;
	}
}
