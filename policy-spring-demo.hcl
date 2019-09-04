path "auth/approle/*" {
  capabilities = [ "create", "read", "update", "delete", "list" ]
}

path "database/*" {
  capabilities = [ "create", "read", "update", "delete", "list" ]
}

