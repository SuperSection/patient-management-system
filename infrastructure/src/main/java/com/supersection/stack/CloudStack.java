package com.supersection.stack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.BootstraplessSynthesizer;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Token;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.CloudMapNamespaceOptions;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerDefinitionOptions;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.FargateService;
import software.amazon.awscdk.services.ecs.FargateTaskDefinition;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.PortMapping;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.PostgresEngineVersion;
import software.amazon.awscdk.services.rds.PostgresInstanceEngineProps;
import software.amazon.awscdk.services.route53.CfnHealthCheck;


public class CloudStack extends Stack {

  private final Vpc vpc;
  private final Cluster ecsCluster;

  public CloudStack(final App scope, final String id, final StackProps props) {
    super(scope, id, props);

    this.vpc = createVpc();

    DatabaseInstance authServiceDB = createDatabase(
        "AuthServiceDB", "auth-service-db"
    );
    DatabaseInstance patientServiceDB = createDatabase(
        "PatientServiceDB", "patient-service-db"
    );

    CfnHealthCheck authServiceDBHealthCheck = createDBHealthCheck(
        authServiceDB, "AuthServiceDBHealthCheck"
    );
    CfnHealthCheck patientServiceDBHealthCheck = createDBHealthCheck(
        patientServiceDB, "PatientServiceDBHealthCheck"
    );

    CfnCluster mskCluster = createMskCluster();

    this.ecsCluster = createEcsCluster();

    FargateService authService = createFargateService(
        "AuthService",
        "auth-service",
        List.of(4005),
        authServiceDB,
        Map.of("JWT_SECRET", "your_jwt_secret")
    );

    authService.getNode().addDependency(authServiceDBHealthCheck);
    authService.getNode().addDependency(authServiceDB);

    FargateService billingService = createFargateService(
        "BillingService",
        "billing-service",
        List.of(4001, 9001),
        null,
        null
    );

    FargateService analyticsService = createFargateService(
        "AnalyticsService",
        "analytics-service",
        List.of(4002),
        null,
        null
    );

    analyticsService.getNode().addDependency(mskCluster);

    FargateService patientService = createFargateService(
        "PatientService",
        "patient-service",
        List.of(4000),
        patientServiceDB,
        Map.of(
            "BILLING_SERVICE_ADDRESS", "billing-service:4001",
            "BILLING_SERVICE_GRPC_PORT", "9001"
        )
    );

    patientService.getNode().addDependency(patientServiceDB);
    patientService.getNode().addDependency(patientServiceDBHealthCheck);
    patientService.getNode().addDependency(billingService);
    patientService.getNode().addDependency(mskCluster);
  }


  private Vpc createVpc() {
    return Vpc.Builder.create(this, "PatientManagmentVPC")
        .vpcName("PatientManagmentVPC")
        .maxAzs(3)
        .build();
  }

  private DatabaseInstance createDatabase(String id, String dbName) {
    return DatabaseInstance.Builder.create(this, id)
        .engine(DatabaseInstanceEngine.postgres(
            PostgresInstanceEngineProps.builder()
                .version(PostgresEngineVersion.VER_17_2)
                .build()))
        .instanceType(
            InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
        .allocatedStorage(20)
        .vpc(vpc)
        .credentials(Credentials.fromGeneratedSecret("admin"))
        .databaseName(dbName)
        .removalPolicy(RemovalPolicy.DESTROY)
        .build();
  }

  private CfnHealthCheck createDBHealthCheck(DatabaseInstance db, String id) {
    return CfnHealthCheck.Builder.create(this, id)
        .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
            .type("TCP")
            .port(Token.asNumber(db.getDbInstanceEndpointPort()))
            .ipAddress(db.getDbInstanceEndpointAddress())
            .requestInterval(30)
            .failureThreshold(3)
            .build())
        .build();
  }

  private CfnCluster createMskCluster() {
    return CfnCluster.Builder.create(this, "MskCluster")
        .clusterName("kafka-cluster")
        .kafkaVersion("2.8.1")
        .numberOfBrokerNodes(1)
        .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
            .instanceType("kafka.m5.large")
            .clientSubnets(vpc.getPrivateSubnets().stream()
                .map(ISubnet::getSubnetId)
                .collect(Collectors.toList()))
            .brokerAzDistribution("DEFAULT")
            .build())
        .build();
  }

  private Cluster createEcsCluster() {
    return Cluster.Builder.create(this, "PatientManagementCluster")
        .vpc(vpc)
        .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
            .name("patient-management.local")
            .build())
        .clusterName("PatientManagementCluster")
        .build();
  }

  private FargateService createFargateService(
      String id,
      String imageName,
      List<Integer> ports,
      DatabaseInstance db,
      Map<String, String> additionalEnvVars
  ) {
    FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, id + "Task")
        .cpu(256)
        .memoryLimitMiB(512)
        .build();

      ContainerDefinitionOptions.Builder containerOptions = ContainerDefinitionOptions.builder()
        .image(ContainerImage.fromRegistry(imageName))
        .portMappings(ports.stream()
            .map(port -> PortMapping.builder()
                .containerPort(port)
                .hostPort(port)
                .protocol(Protocol.TCP)
                .build())
            .toList())
        .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                .logGroup(LogGroup.Builder.create(this, id + "LogGroup")
                    .logGroupName("/ecs/" + imageName)
                    .removalPolicy(RemovalPolicy.DESTROY)
                    .retention(RetentionDays.ONE_DAY)
                    .build())
                .streamPrefix(imageName)
            .build()));

    Map<String, String> envVars = new HashMap<>();
    // envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost.localstack.cloud:4510, localhost.localstack.cloud:4511, localhost.localstack.cloud:4512");
    envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka-cluster:9092");

    if (additionalEnvVars != null) {
      envVars.putAll(additionalEnvVars);
    }

    if (db != null) {
      envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db".formatted(
              db.getDbInstanceEndpointAddress(),
              db.getDbInstanceEndpointPort(),
              imageName
      ));
      envVars.put("SPRING_DATASOURCE_USERNAME", "admin");
      envVars.put("SPRING_DATASOURCE_PASSWORD",
              db.getSecret().secretValueFromJson("password").toString());
      envVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
      envVars.put("SPRING_SQL_INIT_MODE", "always");
      envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");
    }

    containerOptions.environment(envVars);
    taskDefinition.addContainer(imageName + "Container", containerOptions.build());

    return FargateService.Builder.create(this, id)
        .cluster(ecsCluster)
        .taskDefinition(taskDefinition)
        .assignPublicIp(false)
        .serviceName(imageName)
        .build();
  }


  public static void main(final String[] args) {
    App app = new App(AppProps.builder().outdir("./cdk.out").build());

    StackProps props = StackProps.builder()
        .synthesizer(new BootstraplessSynthesizer())
        .build();

    new CloudStack(app, "cloudstack", props);
    app.synth();
    System.out.println("App synthesizing in progress...");
  }
}
