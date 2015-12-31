# Visitor Detector

An Android app designed to detect and alert the user at a certain time when visiting devices are
detected on their home network. Requires a router running OpenWRT with luci and the `luci-mod-rpc`
package installed.

## Environment variables necessary during build

While work proceeds with adding configurability to the app, the following environment variables
need to be set at build-time for the app to be useful:

- `VISITOR_DETECTOR_ROUTER_IP_ADDRESS`: the router's IP address (e.g., `192.168.1.1`)
- `VISITOR_DETECTOR_USERNAME`: the username to login into the router with
- `VISITOR_DETECTOR_PASSWORD`: the password to login into the router with
