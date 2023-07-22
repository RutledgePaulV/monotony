terraform {
  required_providers {
    tls = {
      source = "hashicorp/tls"
    }
  }
}

resource "tls_private_key" "this" {
  algorithm = "ED25519"
}

resource "tls_self_signed_cert" "this" {
  private_key_pem = tls_private_key.this.private_key_pem

  dns_names = var.domain_names

  subject {
    common_name  = "monotony.io"
    organization = "Monotony"
  }

  validity_period_hours = 12

  allowed_uses = [
    "key_encipherment",
    "digital_signature",
    "server_auth",
  ]
}

