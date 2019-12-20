# fortnox4j-cli
Fortnox4j Simple Command Line Interface

## Dependencies
This project depends on [fortnox4j](https://github.com/notima/fortnoxj), which must be built and installed first unless you're using a non-SNAPSHOT version.

## Usage

Get access token from API-code

```
Fortnox4jcli -a [accessToken] -s [clientSecret]
```

General usage

```
usage: Fortnox4Jcli
 -a,--apicode <arg>       The API-code recieved from the Fortnox client
                          when adding the integration. Must be combined
                          with -s
 -c,--cmd <arg>           Command. Available commands: getAccessToken,
                          getCustomerList, listUnpaidCustomerInvoices
 -s <arg>                 Client Secret. This is the integrator's secret
                          word.
 -t,--accesstoken <arg>   The access token to the Fortnox client
 ```