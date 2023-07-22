variable "name" {
  type = string
}

variable "contents" {
  type    = map(string)
  default = {}
}

variable "domain_names" {
  type = list(string)
}