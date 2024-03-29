terraform {
  required_version = "1.1.5"
  required_providers {
    tls = {
      source = "hashicorp/tls"
      version = "4.0.4"
    }
    local = {
      source = "hashicorp/local"
      version = "2.4.0"
    }
  }
}

provider "local" {

}

provider "tls" {

}

module "project" {
  source = "../../modules/project"
  domain_names = ["dev.monotony.io"]
  name         = "monotony-${basename(path.root)}"
}