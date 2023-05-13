output "vpc_id" {
  value = aws_vpc.account_vpc.id
}

output "public_subnet_ids" {
  value = toset(values(aws_subnet.public_subnets)[*].id)
}

output "public_subnets_ids_by_az" {
  value = {
  for k, v in aws_subnet.public_subnets: "${k}" => v.id
  }
}

output "private_subnet_ids" {
  value = toset(values(aws_subnet.private_subnets)[*].id)
}

output "private_subnets_ids_by_az" {
  value = {
  for k, v in aws_subnet.private_subnets: "${k}" => v.id
  }
}

output "database_subnet_ids" {
  value = toset(values(aws_subnet.database_subnets)[*].id)
}

output "database_subnets_ids_by_az" {
  value = {
  for k, v in aws_subnet.database_subnets: "${k}" => v.id
  }
}

output "private_route_table_ids" {
  value = toset(values(aws_route_table.private_route_table)[*].id)
}

output "database_route_table_ids" {
  value = toset([
    aws_route_table.database_route_table.id
  ])
}

output "public_route_table_ids" {
  value = toset([
    aws_route_table.public_route_table.id
  ])
}