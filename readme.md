Monotony is a CLI tool for efficiently managing large Terraform monorepos.


Features:

- Keep track of which plans have been applied on various revisions of code and easily get things back in sync.
- Auto-approve conditions enable safe unsupervised applications across multiple plans.
- Automatically upgrade module references.
- Automatically upgrade provider versions.
- Execute plans concurrently.


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