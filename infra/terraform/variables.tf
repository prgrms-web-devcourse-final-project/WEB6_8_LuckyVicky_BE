variable "region" {
  description = "region"
  default     = "ap-northeast-2"
}

variable "prefix" {
  description = "Prefix for all resources"
  default     = "team07-morimori"
}

variable "morimori_domain" {
  description = "morimori domain"
  default     = "api.mori-mori.store"
}

variable "team" {
  description = "tag team name"
  type        = string
  default     = "devcos-team07"
}

variable "github_repo_owner" {
  description = "The owner of the GitHub repository (user or organization)."
  type        = string
  default     = "prgrms-web-devcourse-final-project"
}

variable "github_repo_name" {
  description = "The name of the GitHub repository."
  type        = string
  default     = "WEB6_8_LuckyVicky_BE"
}