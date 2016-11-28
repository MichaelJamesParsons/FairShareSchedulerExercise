# Fair Share Scheduler

This is an implementation of the fair share scheduler for the CSC 400 Fall 2016 semester.

## Disclaimer

This source code is a part of a university assignment and is to be used for academic purposes only. Students who have been assigned a similar task should not review this source code as it may be considered cheating at some institutions. The files `Sim.java`,  `Asm.java`, and all assets in the `/data` directory were provided as part of the assignment. I do not claim ownership of those files nor do they reflect the quality of my work.

## Basic Usage

### Learn the API

The fair share scheduler is executed through a commandline interface, which accepts 3 required arguments:

- `-w` - The default weight of each process (decimal 0 < x < 1).
- `-p` - The default priority of each process (int x > 0).
- `-f` - A list of `.pexe` files (delimited by a space) to be executed, followed by their group ID. For example, if your file is `comp.pexe` with a group ID of `5`, your `-f` argument would look like this: `-f comp.pexe:5`.

### Execute the Scheduler (example)

    java Sim -w .5 -p 60 -f comp.pexe:1 comp2.pexe:2 comp3.pexe:1 ...
    
This scheduler assumes your `.pexe` files are in the same directory at which you are executing the scheduler. If they are located elsewhere, you can simply prepend the relative path to the file in your arguments:

    java Sim -w .5 -p 60 -f ../some/other/location/comp.pexe:1
    
### That's It!

Upon executing the scheduler, the script will print each action executed by the scheduler (start/stop/block process) in your console.