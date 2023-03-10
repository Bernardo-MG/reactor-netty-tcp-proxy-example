# Proxy Structure

The proxy redirects requests and responses between a client and a server, in such a way that the user never notices anything. Behind the scenes it actually has its own server and clients, but in reverse, as these are the ones capturing any request for redirecting to the real server.

![Proxying requests](./images/usecase_diagram.drawio.png)

To make sure these requests are proxied correctly there is a bridge between the proxy server and proxy client. This will take care of making sure the redirection works correctly, and also serves as an extension point for any additional logic which may be required.

## Components

In the implementation the proxy revolves around ReactorNettyTcpProxyServer, which implements the Server interface. With this interface it is possible start and stop the server at any moment, hiding the fact that it is actually a proxy.

Inside the proxy server contains an instance of the proxy client, ReactorNettyProxyClient. This can create new connections to the real server as needed.

The third main component is for the bridge, ProxyConnectionBridge. This will take two connections, one being the client connection to the proxy server, and the other the proxy client connection to the real server. Both will be connected to redirect messages between them.

The proxy listener is just for the CLI, to print messages based on what is going on in the proxy.

![Proxy class structure](./images/proxy_global_classes.drawio.png)

### Connections

Once the proxy server is started it keeps listening for new connections until stopped. Each connection will start the proxying loop, which also starts a proxy client connection, bridges them and then keeps handling messages for as long as the connection is open.

![Connection loop](./images/flow_diagram.drawio.png)
