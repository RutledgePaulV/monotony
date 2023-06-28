terraform {
  required_version = "> 1.2.0"
}

module "my_module" {
  source = "./testing"
  thing = ""
}

module "git_example" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.0.0"
}