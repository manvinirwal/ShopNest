# ShopNest catalog service
- Run unit tests with: mvn -q test -Dtest='ProductMapperTest'
- Never run plain `mvn test`: the context test needs MySQL.
- Controller -> IProductService -> repository. DTOs only in and out of controllers.
- Constructor injection only.
