# Mermaid Chart extension for IntelliJ IDEA

This extension is a Tool for visualizing and editing Mermaid diagrams in IntelliJ IDEA. The extension enables developers to view and edit diagrams stored in Mermaid Chart from IntelliJ IDEA. With the integration to the Mermaid Chart service, this extension allows users to attach diagrams to their code and to gain quick access to updating diagrams.

Simplify your development workflow with the Mermaid Chart extension.

## Features

In the explorer view under the MERMAIDCHART section you will find all the diagrams you have access to. When you click on a diagram, that diagram will be inserted into the code editor at the position of the cursor. To get the latest changes of diagrams from Mermaid Chart, click on the button named Refresh in the explorer view.

## Requirements

The Mermaid Chart extension for IntelliJ IDEA seamlessly integrates with the Mermaid Chart service, requiring an account to use. Choose between the free tier (limited to 5 diagrams) or the pro tier (unlimited diagrams). Collaborate by setting up teams and sharing diagrams within your development organisation. Simplify diagram management and enhance your workflow with Mermaid Chart for IntelliJ IDEA.

## Build Process

1. Import project to IntelliJ IDEA
2. Run "Execute Gradle Task" (you can find it by pressing Shift+Shift or in Gradle tab to the right of editor)
3. Run "gradle buildPlugin"
4. Then the plugin .zip can be found in $PROJECT_FOLDER/build/distribution folder

* In case of any troubles try to Invalidate Caches and Restart 