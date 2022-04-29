# SET08122 Coursework: Dilloid's Sudoku

To compile this application, open the provided source code in the IntelliJ IDEA
development environment. Alternatively, you can clone the project from GitHub at 
https://github.com/Dilloid/set08122. The project may be compatible with other IDEs,
but results may differ depending on the environment. The application can be 
built and run inside the environment, but I recommend running `maven package` 
to generate a .jar file.

The jar can then be executed from any command line by typing `java -jar DilloidSudoku-1.0.jar`
inside the folder containing the file. **This project requires Java 11 or later!**

This application supports saving and loading sudoku puzzles from file. A `saves` directory will 
automatically be created in the same location as the jar file. There is currently a bug when 
running the jar from the default `target` directory, which will generate the `saves` folder 
at `target\classes\saves` instead of just `target\saves`. I am looking into solving this issue.

For now, I recommend moving the jar to a separate folder outside of the project directory and running it from there.

Video Demo: https://drive.google.com/file/d/10Zb3OeVx9kUwwAfpMHqwFfhwDhfiOose