# Getting Started

To start a new project, you can use the [kgpu-template repository](https://github.com/kgpu/kgpu-template).
It allows you to get a basic project setup by only needing to change a few files.

## Cloning the project

### Using Github

1. Navigate to the [kgpu-template repository](https://github.com/kgpu/kgpu-template)
2. Click the `Use This Template` Button

For more info, see [Creating a repository from a template](https://docs.github.com/en/github/creating-cloning-and-archiving-repositories/creating-a-repository-from-a-template)

### No Github Account

1. Navigate to the [kgpu-template repository](https://github.com/kgpu/kgpu-template)
2. Click the `Clone or Download` (or `Code`) dropdown
3. Select the `Download Zip` option

## Editing the project settings

Once you have the project copied, then you can open it in any text editor,
and then open the `gradle.properties` file.

You can edit the file to change the name of the project, the project's group, and the version of KGPU to use.

Here is an example properties file:

```properties
kotlin.code.style=official
kgpuVersion=0.1.0-SNAPSHOT
projectName=ProjectName
projectGroup=your.group.here
projectVersion=0.1.0
desktopMainClass=DesktopKt
```

## Editing Website

The last part of setup is setting up the index.html page to use.

Open the `src/jsMain/resources/index.html` file

Then change the ProjectName in the script source, to the name you set in
the `gradle.properties` file.

```html
<script>
     if (!navigator.gpu) {
        document.body.className = 'error';
        document.getElementById('content').hidden = true
        document.getElementById('errorMessage').hidden = false
    }
</script>
<script src="ProjectName.js"></script> <!-- Change this line! -->
```

## Start Coding

You can now start working on your project! There are three kotlin files in the template: `Application`, `Desktop`,
and `Browser`

`src/commonMain/kotlin/Application.kt:`
This is the code that is shared between all of the platforms.

`src/jsMain/kotlin/Browser.kt:`
This is the code that will be compiled into javascript for the browser

`src/jvmMain/kotlin/Desktop.kt:`
This is the code that will be compiled into java classes and executed on the Desktop via the JVM.

## Running the application

To run on the desktop:

```bash
gradlew runJvm
```

To build the examples for the Web:

```bash
gradlew jsBrowserDistribution
```

And you can start a static file server for the web:

```bash
gradlew startWebServer
```

And then navigate to [http://localhost:8080/index.html](http://localhost:8080/index.html)
