(ns monotony.cli.interface
  (:require [monotony.cli.commands :as commands]))


(def ListPlansCommand
  {:command     "list"
   :description "List the directories containing terraform plans."
   :run         #'commands/list-plans})

(def SummarizePlanCommand
  {:command     "summarize"
   :description "Summarize the selected terraform plans."
   :run         identity})

(def PlanCommand
  {:command     "plan"
   :description "Use terraform to determine execution plans."
   :run         identity})

(def ApplyCommand
  {:command     "apply"
   :description "Use terraform to apply execution plans."
   :run         identity})

(def UpgradeCommand
  {:command     "upgrade"
   :description "Upgrade versions of terraform, modules, and providers."
   :run         identity})

(def ListModulesCommand
  {:command     "list"
   :description "List the directories containing terraform modules."
   :run         identity})

(def SummarizeModulesCommand
  {:command     "summarize"
   :description "Summarize the selected terraform modules."
   :run         identity})

(def PlansNamespace
  {:command     "plans"
   :description "Commands for managing terraform plans."
   :subcommands #{PlanCommand
                  ApplyCommand
                  UpgradeCommand
                  ListPlansCommand
                  SummarizePlanCommand}})

(def ModulesNamespace
  {:command     "modules"
   :description "Commands for managing terraform modules."
   :subcommands #{ListModulesCommand UpgradeCommand SummarizeModulesCommand}})

(def VersionCommand
  {:command     "version"
   :description "Prints the version of monotony."
   :run         identity})

(def MainCommand
  {:command     "monotony"
   :description "A tool for managing terraform plans and modules."
   :subcommands #{PlansNamespace
                  ModulesNamespace
                  VersionCommand}})