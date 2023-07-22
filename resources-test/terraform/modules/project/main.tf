
module "certs" {
  source       = "../certs"
  domain_names = var.domain_names
}

module "files" {
  source = "../files"
  contents = {}
}