variable "region" {
  description = "region"
  default     = "ap-northeast-2"
}

variable "prefix" {
  description = "Prefix for all resources"
  default     = "team7-morimori"
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