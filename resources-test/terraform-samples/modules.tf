module "my_module" {
  source = "."
}

module "my_module_using_another_module" {
  source = "./test"
  attribute = module.my_module.something
}

