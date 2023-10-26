package com.example;

import io.micronaut.aws.cdk.function.MicronautFunction;
import io.micronaut.aws.cdk.function.MicronautFunctionFile;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.options.BuildTool;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.CfnFunction;
import software.constructs.IConstruct;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Tracing;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;
import java.util.HashMap;
import java.util.Map;

public class AppStack extends Stack {

    public AppStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public AppStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        Map<String, String> environmentVariables = new HashMap<>();
        Function function = MicronautFunction.create(ApplicationType.FUNCTION,
                false,
                this,
                "micronaut-function")
                .runtime(Runtime.JAVA_17)
                .handler("com.example.FunctionRequestHandler")
                .environment(environmentVariables)
                .code(Code.fromAsset(functionPath()))
                .timeout(Duration.seconds(10))
                .memorySize(2048)
                .logRetention(RetentionDays.ONE_WEEK)
                .tracing(Tracing.DISABLED)
                .architecture(Architecture.X86_64)
                .build();
        IConstruct defaultChild = function.getNode().getDefaultChild();
        if (defaultChild instanceof CfnFunction) {
            CfnFunction cfnFunction = (CfnFunction) defaultChild;
            cfnFunction.setSnapStart(CfnFunction.SnapStartProperty.builder()
                .applyOn("PublishedVersions")
                .build());
        }
    }

    public static String functionPath() {
        return "../app/target/" + functionFilename();
    }

    public static String functionFilename() {
        return MicronautFunctionFile.builder()
            .graalVMNative(false)
            .version("0.1")
            .archiveBaseName("demo")
            .buildTool(BuildTool.MAVEN)
            .build();
    }
}