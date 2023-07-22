terraform {
  required_providers {
    local = {
      source = "hashicorp/local"
    }
  }
}

resource "local_file" "this" {
  for_each = var.contents
  content  = each.value
  filename = "${path.root}/${each.key}"
}