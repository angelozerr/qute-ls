# Good practice to write Qute template

This page list good practice that IMHO I think we should follow to write Qute template according to my experinces with another template engine like Velocity or Freemarker.

When you write a template, you know the context:

 * the data model that you wish to use to render the template.
 * the structure of your project (where templates are stored, where some txt files for documentation are stored, etc)
 
But please keep in mind, that other developers can contribute to your project and that's why
it's very important to help them to contribute to update your existing template.

It's the reason why I think it's important to follow some rules.

I will use *contributor* to name the developers who have not written the templates.

## Qute template file extension.

Qute template file extension should use `.qute.` :

 * for txt file, template should ends with `.qute.txt`.
 * for json file, template should ends with `.qute.json`.
 * for html file, template should ends with `.qute.html` or `.qute.htm`.
 * for yaml file, template should ends with `.qute.yaml` or `.qute.yml`.
 
 ### Why?
 
  * *contributor* discovers the project and want to search the Qute templates. If it search `.qute.txt` in the project, it will find all Qute templates. If Qute template is named with only `.txt`, the search result will return Qute templates, but another txt files like documentation.
  
  * for *tooling point of view*, it's more easy to associate relevant Qute language support (completion, syntax coloration, validation, etc) if file ends with `.qute.txt`.
  
## Use parameter declaration.

Qute template provides a syntax to declare expected data model with [parameter declaration](https://quarkus.io/guides/qute#template-parameter-declarations). 

IMHO I think all Qute templates should use it.

 ### Why?
 
Imagine you have the following Qute template:

```
Name: {item.name}
```

And *contributor* need to update it.

  * *contributor* don't know what is `item` type? It must search in the project where the `item` is set as data model (by searching `@CheckedTemplate` or by searching the data model populate if Qute is used in standalone mode. This search can be horrible if project is big.
  * for *tooling point of view* it's impossible to manage completion for `name` field, check `name` is a valid field, etc.
  
In another word, I think it's better to write the template with parameter declaration:

```
{@org.acme.Item item}
Name: {item.name}
```
 
 * for *contributor*, it will help him to know which type is expected in the template and by reading Java `org.acme.Item`, it will see what fields are allowed and will able to add another fields easily.
 * for *tooling point of view*, completion, validation, hover, jump to Java field definition for `name` field could be supported.
