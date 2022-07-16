#_(neo/defquery
  create-user
  "CREATE (u:user $user)")

#_(neo/defquery list-users
                         "MATCH (u:user) RETURN u as user")