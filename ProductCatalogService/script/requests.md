| # | Request | Expect |
|---|---|---|
| 1 | POST {auth}/auth/signup {"email":"admin@shopnest.com","password":"Pass@12345"} | 201 |
| 2 | POST {auth}/auth/login (same body) | 200 and a token |
| 3 | GET {catalog}/products/1 | 404 when the shelf is empty |
| 4 | POST {catalog}/products with no token | 401 |
| 5 | POST {catalog}/products with the token, body {"name":"Handmade Candle","price":199.00,"category":{"name":"home-decoration"}} | 201 |
| 6 | DELETE {catalog}/products/<id from request 5> with the same token | 403 for a CUSTOMER |
