package com.supersection.stack;

import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.BootstraplessSynthesizer;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Token;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.PostgresEngineVersion;
import software.amazon.awscdk.services.rds.PostgresInstanceEngineProps;
import software.amazon.awscdk.services.route53.CfnHealthCheck;


public class CloudStack extends Stack {

  private final Vpc vpc;

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
