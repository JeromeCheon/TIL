const express = require("express");
const { ApolloServer, gql } = require("apollo-server-express");
const fs = require("fs").promises;
const { mergeResolvers } = require("@graphql-tools/merge");
const userResolver = require("./resolvers/user-resolvers");
const cartResolver = require("./resolvers/cart-resolvers");

const app = express();
const port = 8081;

const schemaFiles = [
  "./schemas/query.graphql",
  "./schemas/mutation.graphql",
  "./schemas/subscription.graphql",
  "./schemas/product.graphql",
  "./schemas/user.graphql",
  "./schemas/cart.graphql",
];

const resolvers = mergeResolvers([userResolver, cartResolver]);

async function startServer() {
  const schemas = await Promise.all(
    schemaFiles.map((file) => fs.readFile(file, "utf-8")),
  );
  const server = new ApolloServer({ typeDefs: schemas, resolvers });
  await server.start();
  server.applyMiddleware({ app });

  app.listen(port, () => {
    console.log(
      `server is running at http://localhost:${port}${server.graphqlPath}`,
    );
  });
}

startServer();
