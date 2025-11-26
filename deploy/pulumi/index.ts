import * as pulumi from "@pulumi/pulumi";
import * as digitalocean from "@pulumi/digitalocean";

const config = new pulumi.Config();

// Configuration with required values (no defaults - will error if not set)
const dropletName = config.require("dropletName");
const region = config.require("region");
const sshKeyName = config.require("sshKeyName");

// Lookup SSH key by name
const sshKey = digitalocean.getSshKeyOutput({ name: sshKeyName });

// DNS configuration
const domainName = config.require("domainName");      // e.g., "artivisi.id"
const subdomainName = config.require("subdomainName"); // e.g., "akunting"

// Minimum spec: s-1vcpu-2gb ($12/month) - sufficient for 3 users
// JVM needs ~1GB heap, PostgreSQL ~256MB, OS overhead ~512MB
const dropletSize = "s-1vcpu-2gb";

// Create the droplet
const droplet = new digitalocean.Droplet("accounting-app", {
    name: dropletName,
    region: region,
    size: dropletSize,
    image: "ubuntu-24-04-x64",
    sshKeys: [sshKey.id.apply(id => id.toString())],
    tags: ["accounting-app", "production"],
});

// Create firewall
const firewall = new digitalocean.Firewall("accounting-firewall", {
    name: `${dropletName}-firewall`,
    dropletIds: [droplet.id.apply(id => parseInt(id))],
    inboundRules: [
        {
            protocol: "tcp",
            portRange: "22",
            sourceAddresses: ["0.0.0.0/0", "::/0"],
        },
        {
            protocol: "tcp",
            portRange: "80",
            sourceAddresses: ["0.0.0.0/0", "::/0"],
        },
        {
            protocol: "tcp",
            portRange: "443",
            sourceAddresses: ["0.0.0.0/0", "::/0"],
        },
    ],
    outboundRules: [
        {
            protocol: "tcp",
            portRange: "1-65535",
            destinationAddresses: ["0.0.0.0/0", "::/0"],
        },
        {
            protocol: "udp",
            portRange: "1-65535",
            destinationAddresses: ["0.0.0.0/0", "::/0"],
        },
        {
            protocol: "icmp",
            destinationAddresses: ["0.0.0.0/0", "::/0"],
        },
    ],
});

// DNS A record for subdomain pointing to droplet
const dnsRecord = new digitalocean.DnsRecord("accounting-dns", {
    domain: domainName,
    type: "A",
    name: subdomainName,
    value: droplet.ipv4Address,
    ttl: 300,
});

// Outputs
export const dropletIp = droplet.ipv4Address;
export const dropletId = droplet.id;
export const dropletStatus = droplet.status;
export const fqdn = pulumi.interpolate`${subdomainName}.${domainName}`;
