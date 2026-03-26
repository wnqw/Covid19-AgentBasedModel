# An Agent Based Model on Covid-19 Transmission Dynamics

[![PDF](https://img.shields.io/badge/Paper-Paper-CC0000.svg)](https://drive.google.com/file/d/1Vhher3hPqq_Lii5ZnI6hIYY1yFrJMkIs/view?usp=drive_link)

<p align="center">
  <br />
  <img src="assets/teaser.png" width="100%" />
  <br />
</p>

This repository contains an agent-based model (ABM) for COVID-19 transmission dynamics based on the paper:

> **An Agent Based Model on Covid-19 Transmission Dynamics**  
> [Wenqing Wang](https://wenqing-wang.netlify.app/)  
> UC Davis, 2019

## What’s implemented

At a high level this project simulates a spatial SEIR-style process with extra states:

* `Susceptible`: agent can become exposed if it is within `distance` of an infectious agent.
* `Exposed`: exposed countdown (`explosedPhase`) before becoming infectious.
* `Infectious`: infectious countdown (`infectiousPhase`) before recovering or dying.
* `Recovered`: recovered countdown (`recoveredPhase`) before returning to `Susceptible` (reinfection possible).
* `Dead`: removed from the simulation (or left visible if `showDeadAgents` is enabled).

The simulation is driven by `NetworkModel.GUICN` (GUI entry point) and `NetworkModel.Environment` (simulation state). The individual agent logic lives in `NetworkModel.Agent`.

## Spatial + contact rule (infection)

Each simulation step, a susceptible agent:

1. Queries all neighbors within `distance` (agents at exactly within range).
2. Skips exposure if `testing` is enabled and the susceptible is `quarantine` (and also skips if the infectious neighbor is quarantined).
3. For each infectious neighbor:
   * If the two agents are in different groups, exposure uses probability `pExpNoneG`.
   * If both are in the same family group (`Groups.FAMILY`), exposure uses probability `pExpFamilyG`.
4. The base exposure probability is modified by agent factors in `Agent.agentFactors(...)`, including:
   * Age mixing: `youngerP` or `elderP` depending on `elderAgeDefine`.
   * Supplements (`takeSupplements` random flag) reduce exposure by `(1 - supplementP)`.
   * Masks (boolean `mask`) reduce exposure by `(1 - maskP)`.
   * Optional mutation: may increase exposure by `(1 + mutationLevel)` with probability `mutationRate` (guarded so the result does not exceed 1).
   * Optional vaccination: if `vaccine` is enabled and the agent is vaccinated, exposure becomes 0 after the vaccine takes effect (`countdownVacTakesEffect`).

## Disease progression (exposed -> infectious -> recover/death)

Countdowns are handled per-agent:

* Exposed countdown: `explosedPhase` (note: spelling matches the code as `explosedPhase`).
* Infectious countdown: `infectiousPhase`.
* Recovered countdown: `recoveredPhase`.

When the infectious countdown reaches 0:

* If `age >= elderAgeDefine`, death happens with probability `elderDeathRate`; otherwise the agent recovers.
* If `age < elderAgeDefine`, death happens with probability `deathRate`; otherwise the agent recovers.

Recovered agents eventually return to `Susceptible` and `setInfectiousEnd()` is called (this is used by the provided `Experimenter` logic for dynamic R0 calculation, though R0 output is currently commented out in `Experimenter`).

## Groups / networks (family)

Agents are assigned a `Groups` value by `Environment.decideGroup()`:

* `Groups.FAMILY` is selected with probability 0.5.
* Otherwise group is `null` (treated as “not family” in group logic).

If `showNetworks` is enabled:

* `Environment.makeNWG()` builds a `NetworkGroup` containing family agents only.
* The network is generated with `randomNetworkMeanK(...)` using `meanK`.
* `GUICN` visualizes this spatial network (edges + agent positions).

If an agent dies and `showNetworks` is enabled, it is removed from the network group as well.

## Movement

Agents move in continuous 2D space:

* Movement happens with probability controlled by `active`.
* Step direction:
  * If `gaussian` is true, `GaussianStep()` uses `randomOrientedGaussianStep(...)`.
  * Otherwise `UniformStep()` uses `randomOrientedUniformStep(...)`.
* Movement step size is controlled by `stepSize`.
* `rotation` affects movement orientation sampling.

If `testing` is enabled:

* Quarantined agents follow the `quarantine()` logic (they are kept in place while the countdown runs).

## Optional interventions

The following switches are controlled by booleans in `NetworkModel.Environment` and expected to be provided via `script.txt` for parameter sweeps:

* Social approval / masking:
  * If `SocApproval` is enabled, `Agent.socApproval()` may turn the agent’s `mask` on when their `approval` drops below `approvalBaseline`.
  * Interactions can apply a penalty (`socPenalty`) to agents without masks when the other agent has a mask.
* Testing and quarantine:
  * If `testing` is enabled, `Agent.testing()` toggles `quarantine` for infectious agents after `countdownTest`.
  * Quarantine lasts for `countdownQuarantine`.
* Vaccine:
  * If `vaccine` is enabled, vaccination proceeds in two doses.
  * Dose uptake is probabilistic and limited by `vaccineCoverage`.
  * After dose 2 is received, exposure becomes 0 after `countdownVacTakesEffect` (unless the code path is still on the countdown).
  * If `elderFirst` is enabled, only elders receive vaccination first (younger agents are blocked until elders are eligible).
* Mutation:
  * If `mutation` is enabled, mutation can increase an agent’s exposure probability using `mutationRate` and `mutationLevel`.

## GUI entry point

Run the GUI main in `NetworkModel.GUICN`:

* `NetworkModel/GUICN.java` contains `public static void main(String[] args)` which calls `GUICN.initialize(...)`.

The GUI expects a MASON-style `index.html` next to the simulation output:

* Source: `PandemicModel/src/NetworkModel/index.html`
* Compiled output (existing): `PandemicModel/bin/NetworkModel/index.html`

## Parameter sweeps + outputs

This project is set up for parameter sweeps using MASONplus-style `SimStateSweep` / `GUIStateSweep`.

Two configuration files are provided inside the nested project directory `PandemicModel/`:

* `PandemicModel/runTimeFile.txt`
  * Points to `script.txt` via `Scriptname: script.txt`
  * Defines the output folder and file name (`DataFolder: data`, `DataFile: results.txt`)
  * Defines column headers (see `ColumnHeaders:` line)
  * Controls behavior like `Precision:` and `stopWhenNoAgents:`
* `PandemicModel/script.txt`
  * Defines the parameters for `NetworkModel.Environment`
  * Supports sweeping up to 3 parameters by listing comma-separated values after a parameter (for example: `public boolean testing = false, true;`)

Data output location:

* `PandemicModel/data/` (based on `runTimeFile.txt`)

What gets sampled:

* `NetworkModel.Experimenter` samples agent counts every `dataSamplingInterval` steps and writes fractions of the total population:
  * `total`
  * `susceptible`, `exposed`, `infected`, `recovered`, `dead`
  * `wearMask`
  * `expByFam`, `expByNoneFam`

## Project dependencies (important)

This repository is an Eclipse-era project that expects the external MASON libraries on the classpath.

Download MASON here: `https://people.cs.gmu.edu/~eclab/projects/mason/`

The module definition indicates dependencies on:

* `MASON`
* a `MASONplus...` variant (the project references plus modules via `.classpath` / `.iml`)
* Java (configured for Java 12 in the `.classpath` / `.iml`)

To build/run:

* Import the nested `PandemicModel/` folder as an Eclipse project (or otherwise ensure the `MASON` and `MASONplus` source/modules are available on the classpath).

