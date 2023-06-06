variable "thing" {
  type = string
}

module "git_example" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.0.0"
}