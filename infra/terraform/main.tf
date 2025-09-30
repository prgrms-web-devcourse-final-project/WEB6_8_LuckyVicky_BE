terraform {
  // 테라폼으로 AWS 외에 다른 것도 가능한데,
  // AWS를 다루기 위해 AWS 라이브러리 불러옴
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
  }
}

# AWS 설정 시작
provider "aws" {
  region = "ap-northeast-2"
}
# AWS 설정 끝

# VPC 설정 시작
resource "aws_vpc" "vpc_1" {
  cidr_block = "10.0.0.0/16"

  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "morimori-vpc-1"
  }
}