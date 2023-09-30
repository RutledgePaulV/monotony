Monotony is a CLI tool for efficiently managing large Terraform monorepos.

Current Features:
- Parse Terraform into a data oriented AST
- Support querying the AST
- Summarize plans and modules
- Support automatic downloading/caching of the correct terraform versions and provider versions to achieve fast
  applications.


Desired Features:

- Keep track of which plans have been applied on various revisions of code and easily get things back in sync.
- Automatically upgrade module references.
- Automatically upgrade provider versions.
- Execute plans concurrently.
- Approve distinct "changes" across a set of plans so that you can safely apply them all with less redundant diffing.
- Generate graphs of plans and modules
- Allow writing validated terraform using edn / clojure functions instead of hcl.
- Render a data AST into HCL.
- Support an "iterative discovery" mode that applies resources in dependency order to avoid "for_each" over an unknown
  quantity errors.
- Support serializing a terraform "context" so it could be stored in a database and used as part of an application.

Installation:

```bash
./install.sh
```

Usage:

```text
$ monotony --help
NAME:
 monotony - A supplementary cli tool for users of Terraform.

USAGE:
 monotony [global-options] command [command options] [arguments...]

VERSION:
 0.1.0-SNAPSHOT

COMMANDS:
   list-plans           List the available terraform plans within the current directory.
   list-modules         List the available terraform modules within the current directory.

GLOBAL OPTIONS:
   -?, --help
```