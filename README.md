# TGAImageConverter
A simple script for quickly converting all .TGA files in a folder to a format

Powered with maven.. so :
```sh
mvn clean compile
mvn exec:java -Dexec.mainClass="infraimageconverter.InfraImageConverter" -Dexec.args="-c <cores> -f <format> -i <input_path> -o <output_path>"
```

> :warning: **Format that you can use** is handled by ImageIO from java.. if you want to modify it to handle an another format that isn't supported.. you will have to register plugin to ImageIO.. hf anyways
