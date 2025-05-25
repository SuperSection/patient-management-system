package com.supersection.stack;

import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.BootstraplessSynthesizer;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;


public class CloudStack extends Stack {

  private final Vpc vpc;

  public CloudStack(final App scope, final String id, final StackProps props) {
    super(scope, id, props);

    this.vpc = createVpc();
  }


  private Vpc createVpc() {
    return Vpc.Builder.create(this, "PatientManagmentVPC")
        .vpcName("PatientManagmentVPC")
        .maxAzs(3) // Default is all AZs in the region
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
