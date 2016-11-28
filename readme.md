# Fair Share Scheduler

This is an implementation of the fair share scheduler for the CSC 400 Fall 2016 semester.


## Basic Usage

### Step 1 Generate Mock Processes

First, compile `Asm.java`. Then, in the same directory where the `asm.class` file is generated, execute `java Asm ***.pasm`, where "***" is the name of a `.pasm` file. This will have to be executed for each individual `.pasm` file.

After you complete this step, you should have a list of `.pexe` files, where the name of each file will match the prefix of your `.pasm` files.

For example `java asm comp.pasm` will create a `comp.pexe` file.

### Step 2 Learn the API

Once the `.pexe` files have been generated, compile this project.

The fair share scheduler is executed through a commandline interface, which accepts 3 required arguments:

- `-w` - The default weight of each process.
- `-p` - The default priority of each process.
- `-f` - A list of `.pexe` files (delimited by a space) to be executed, followed by their group ID. For example, if your file is `comp.pexe` with a group ID of `5`, your `-f` argument would look like this: `-f comp.pexe:5`.

### Execute the Scheduler (example)

    java Sim -w .5 -p 60 -f comp.pexe:1 comp2.pexe:2 comp3.pexe:1 ...
    
This scheduler assumes your `.pexe` files are in the same directory at which you are executing the scheduler. If they are located elsewhere, you can simply prepend the relative path to the file in your arguments:

    java Sim -w .5 -p 60 -f ../some/other/location/comp.pexe:1