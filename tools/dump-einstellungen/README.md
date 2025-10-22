# Dump Einstellungen

Dumps Gesuchsperiodeneinstellungen of DEV, UAT and PROD for a given Mandant into a CSV.

## Requirements

No dependencies. Tested with Node.js 20.17.0.

## Run locally

```shell
MANDANT=be COOKIE_DEV='<value of JSESSIONID cookie>' COOKIE_UAT='<value of JSESSIONID cookie>' COOKIE_DEMO='<value of JSESSIONID cookie>' COOKIE_PROD='<value of JSESSIONID cookie>' node index.mjs > einstellungen-be.csv
```

## Run on gitlab

Manually run the job `dump-einstellungen`, it should be available in every kind of pipeline. Pass `MANDANT`, `COOKIE_DEV`, `COOKIE_UAT`, `COOKIE_DEMO`, `COOKIE_PROD` as job variable,
as seen above. 

# Resources

- [View, add, edit, and delete cookies in Chrome](https://developer.chrome.com/docs/devtools/application/cookies/)
- [Specifying variables when running manual jobs in gitlab](https://docs.gitlab.com/ee/ci/jobs/#specifying-variables-when-running-manual-jobs)
