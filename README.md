# WinMDj

[win32metadata](https://github.com/microsoft/win32metadata) is a project by Microsoft to document a variety of their
platform APIs. They and others have then taken these API definitions to build language bindings for C#/rust, etc..
This project is an attempt at doing the same for Java and JVM languages.

### A work in progress 
Java Parser for WinMD files such as Microsoft's [Microsoft.Windows.SDK.Win32Metadata](https://www.nuget.org/packages/Microsoft.Windows.SDK.Win32Metadata/#readme-body-tab) platform definitions.

### Building

This project requires Maven and Java 21+ to build.

Building The Libraries
`mvn clean package`

Run the Stand-alone exploration module:
`java -jar winmdj-explore/target/winmdj-explore-1.0.0-SNAPSHOT.jar`