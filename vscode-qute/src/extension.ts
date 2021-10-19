/**
 * Copyright 2019 Red Hat, Inc. and others.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import * as requirements from './languageServer/requirements';

import { VSCodeCommands } from './definitions/constants';

import { CodeLensParams, DidChangeConfigurationNotification, LanguageClientOptions, TextDocumentIdentifier } from 'vscode-languageclient';
import { LanguageClient } from 'vscode-languageclient/node';
import { ExtensionContext, commands, window, workspace, CodeLensProvider, CancellationToken, CodeLens, Event, ProviderResult, TextDocument, languages, Uri } from 'vscode';
import { QuarkusContext } from './QuarkusContext';
import { addExtensionsWizard } from './addExtensions/addExtensionsWizard';
import { createTerminateDebugListener } from './debugging/terminateProcess';
import { generateProjectWizard } from './generateProject/generationWizard';
import { prepareMicroProfileExecutable, prepareQuteExecutable } from './languageServer/javaServerStarter';
import { tryStartDebugging } from './debugging/startDebugging';
import { WelcomeWebview } from './webviews/WelcomeWebview';
import { QuarkusConfig } from './QuarkusConfig';
import { registerConfigurationUpdateCommand, registerOpenURICommand, CommandKind } from './lsp-commands';
import { QBookSerializer } from './quteNotebook/qBookSerializer';
import { QBookController } from './quteNotebook/qBookController';
import { TextEncoder } from 'util';

let languageClient: LanguageClient;
let quteLanguageClient: LanguageClient;

export function activate(context: ExtensionContext) {
  QuarkusContext.setContext(context);
  displayWelcomePageIfNeeded(context);

  context.subscriptions.push(createTerminateDebugListener());

  //context.subscriptions.push(
  //  languages.registerCodeLensProvider([{ scheme: 'file', language: 'java' }], new QuteCodeLensProviderForJavaFile()));

  connectToQuteLS(context).then(() => {
    bindQuteRequest('qute/template/project');
    bindQuteRequest('qute/template/projectDataModel');
    bindQuteRequest('qute/template/javaClasses');
    bindQuteRequest('qute/template/resolvedJavaClass');
    bindQuteRequest('qute/template/javaDefinition');
    bindQuteNotification('qute/dataModelChanged');
    bindQuteRequest('qute/java/codeLens');
  }).catch((error) => {
    window.showErrorMessage(error.message, error.label).then((selection) => {
      if (error.label && error.label === selection && error.openUrl) {
        commands.executeCommand('vscode.open', error.openUrl);
      }
    });
  });

  function bindQuteRequest(request: string) {
    quteLanguageClient.onRequest(request, async (params: any) =>
      <any>await commands.executeCommand("java.execute.workspaceCommand", request, params)
    );
  }

  function bindQuteNotification(notification: string) {
    context.subscriptions.push(commands.registerCommand(notification, (event: any) => {
      quteLanguageClient.sendNotification(notification, event);
    }));
  }

  function bindRequest(request: string) {
    languageClient.onRequest(request, async (params: any) =>
      <any>await commands.executeCommand("java.execute.workspaceCommand", request, params)
    );
  }

  function bindNotification(notification: string) {
    context.subscriptions.push(commands.registerCommand(notification, (event: any) => {
      languageClient.sendNotification(notification, event);
    }));
  }

  registerVSCodeCommands(context);
  registerQuteNotebook(context);
}

export function deactivate() {
}

function displayWelcomePageIfNeeded(context: ExtensionContext): void {
  if (QuarkusConfig.getAlwaysShowWelcomePage()) {
    WelcomeWebview.createOrShow(context);
  }
}

function registerVSCodeCommands(context: ExtensionContext) {

  /**
   * Command for creating a Quarkus Maven project
   */
  context.subscriptions.push(commands.registerCommand(VSCodeCommands.CREATE_PROJECT, () => {
    generateProjectWizard();
  }));

  /**
   * Command for adding Quarkus extensions to current Quarkus Maven project
   */
  context.subscriptions.push(commands.registerCommand(VSCodeCommands.ADD_EXTENSIONS, () => {
    addExtensionsWizard();
  }));

  /**
   * Command for debugging current Quarkus Maven project
   */
  context.subscriptions.push(commands.registerCommand(VSCodeCommands.DEBUG_QUARKUS_PROJECT, () => {
    tryStartDebugging();
  }));

  /**
   * Command for displaying welcome page
   */
  context.subscriptions.push(commands.registerCommand(VSCodeCommands.QUARKUS_WELCOME, () => {
    WelcomeWebview.createOrShow(context);
  }));

  /**
   * Register standard LSP commands
   */
  context.subscriptions.push(registerConfigurationUpdateCommand());
  context.subscriptions.push(registerOpenURICommand());
  registerOpenUriCommand(context);
  registerGenerateTemplateFileCommand(context);
}

function connectToQuteLS(context: ExtensionContext) {
  return requirements.resolveRequirements().then(requirements => {
    const clientOptions: LanguageClientOptions = {
      documentSelector: [
        { scheme: 'file', language: 'qute-html' },
        { scheme: 'file', language: 'qute-json' },
        { scheme: 'file', language: 'qute-yaml' },
        { scheme: 'file', language: 'qute-txt' },
        { scheme: 'untitled', language: 'qute-html' },
        { scheme: 'vscode-notebook-cell', language: 'qute-html' },
        { scheme: 'file', language: 'java' }
      ],
      // wrap with key 'settings' so it can be handled same a DidChangeConfiguration
      initializationOptions: {
        settings: getQuteSettings()
      },
      synchronize: {
        // preferences starting with these will trigger didChangeConfiguration
        configurationSection: ['qute', '[qute]']
      },
      middleware: {
        workspace: {
          didChangeConfiguration: () => {
            quteLanguageClient.sendNotification(DidChangeConfigurationNotification.type, { settings: getQuteSettings() });
          }
        }
      }
    };

    const serverOptions = prepareQuteExecutable(requirements);
    quteLanguageClient = new LanguageClient('qute', 'Qute Support', serverOptions, clientOptions);
    context.subscriptions.push(quteLanguageClient.start());
    return quteLanguageClient.onReady();
  });

  /**
   * Returns a json object with key 'quarkus' and a json object value that
   * holds all quarkus. settings.
   *
   * Returns: {
   *            'quarkus': {...}
   *          }
   */
  function getQuteSettings(): JSON {
    const configQuarkus = workspace.getConfiguration().get('qute');
    let quarkus;
    if (!configQuarkus) { // Set default preferences if not provided
      const defaultValue =
      {
        qute: {

        }
      };
      quarkus = defaultValue;
    } else {
      const x = JSON.stringify(configQuarkus); // configQuarkus is not a JSON type
      quarkus = { quarkus: JSON.parse(x) };
    }
    return quarkus;
  }
}

function registerQuteNotebook(context: ExtensionContext) {
  context.subscriptions.push(
    workspace.registerNotebookSerializer(
      'qbook', new QBookSerializer(), { transientOutputs: true }
    ),
    new QBookController()
  );
}

function registerOpenUriCommand(context: ExtensionContext) {
  context.subscriptions.push(commands.registerCommand('qute.command.open.uri', async (uri?: string) => {
    commands.executeCommand('vscode.open', Uri.parse(uri));
  }));
}

function registerGenerateTemplateFileCommand(context: ExtensionContext) {
  context.subscriptions.push(commands.registerCommand('qute.command.generate.template.file', async (info?) => {
    const templateContent: string = await commands.executeCommand('qute.command.generate.template.content', info);
    const uri = info.templateFileUri;
    const fileUri = Uri.parse(uri);
    await workspace.fs.writeFile(fileUri, new TextEncoder().encode(templateContent));
    window.showTextDocument(fileUri, { preview: false });
  }));
}

/*class QuteCodeLensProviderForJavaFile implements CodeLensProvider {
  onDidChangeCodeLenses?: Event<void>;
  provideCodeLenses(document: TextDocument, token: CancellationToken): ProviderResult<CodeLens[]> {
   /* const params: CodeLensParams = {
      textDocument: TextDocumentIdentifier.create(document.uri.toString())
    };*/
/*  const uri = document.uri.toString();
  return commands.executeCommand("java.execute.workspaceCommand", 'qute/java/codeLens', {uri : uri });
}
}*/

