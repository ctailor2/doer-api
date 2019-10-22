package com.codegenerator.poc

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.lang.model.element.Modifier

class CodeGeneratorPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def extension = project.extensions.create('codeGenerator', CodeGeneratorPluginExtension)
        project.task('generateSources') {
            doLast {
                TypeSpec typeEnum = TypeSpec
                        .enumBuilder("Type")
                        .addModifiers(Modifier.PUBLIC)
                        .addEnumConstant("READ_ONLY")
                        .addEnumConstant("ENUMERATED")
                        .addEnumConstant("TEXT")
                        .build()
                def typeEnumType = TypeVariableName.get(typeEnum.name)
                def typeEnumVariableName = typeEnum.name.toLowerCase()
                TypeSpec attributeNameEnum = TypeSpec
                        .enumBuilder("AttributeName")
                        .addModifiers(Modifier.PUBLIC)
                        .addField(FieldSpec.builder(typeEnumType, typeEnumVariableName, Modifier.PRIVATE, Modifier.FINAL).build())
                        .addMethod(MethodSpec.constructorBuilder()
                            .addParameter(typeEnumType, typeEnumVariableName)
                            .addStatement("this.\$N = \$N", typeEnumVariableName, typeEnumVariableName)
                            .build())
                        .addEnumConstant(
                            "FAT",
                            TypeSpec.anonymousClassBuilder("\$L", "READ_ONLY").build()
                        )
                        .addEnumConstant(
                            "BRAND",
                            TypeSpec.anonymousClassBuilder("\$L", "TEXT").build()
                        )
                        .addEnumConstant(
                            "PRIVATE_LABEL",
                            TypeSpec.anonymousClassBuilder("\$L", "ENUMERATED").build()
                        )
                        .build()
                [
                        JavaFile.builder("$extension.packageName", typeEnum).build(),
                        JavaFile.builder("$extension.packageName", attributeNameEnum)
                                .addStaticImport(ClassName.get(extension.packageName, typeEnum.name), "*")
                                .build(),
                ].each {
                    it.writeTo(project.mkdir("$project.buildDir/${extension.generatedSourcesDirectory}"))
                }
            }
        }
    }
}
