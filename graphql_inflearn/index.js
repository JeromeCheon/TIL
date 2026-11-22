const express = require("express");
const { ApolloServer, gql } = require("apollo-server-express");
const fs = require("fs").promises;
const { mergeResolvers } = require("@graphql-tools/merge");
const { makeExecutableSchema } = require("@graphql-tools/schema");
const userResolver = require("./resolvers/user-resolvers");
const cartResolver = require("./resolvers/cart-resolvers");
const productResolver = require("./resolvers/product-resolvers");
const searchResolver = require("./resolvers/search-resolvers");
const { WebSocketServer } = require("ws");
const { useServer } = require("graphql-ws/use/ws");

const { PubSub } = require("graphql-subscriptions");

const pubsub = new PubSub();

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

const resolvers = mergeResolvers([
  userResolver,
  cartResolver,
  productResolver,
  searchResolver,
]);

async function startServer() {
  const schemas = await Promise.all(
    schemaFiles.map((file) => fs.readFile(file, "utf-8")),
  );
  const schema = makeExecutableSchema({ typeDefs: schemas, resolvers });

  const server = new ApolloServer({ schema, context: { pubsub } });
  await server.start();
  server.applyMiddleware({ app });

  const httpServer = app.listen(port, () => {
    console.log(
      `server is running at http://localhost:${port}${server.graphqlPath}`,
    );
  });

  const wsServer = new WebSocketServer({
    server: httpServer,
    path: "/graphql",
  });

  useServer({ schema, context: { pubsub } }, wsServer);
}

startServer();
