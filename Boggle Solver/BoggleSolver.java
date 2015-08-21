/** The following program solves a Boggle pozzle utilizing recursion.
 * @author Alberto Mizrahi
 * */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

interface Positionable {
    int getRow();
    int getColumn();
    List<Positionable> getNeighbors();
    char getValue();
}

class Puzzle
{
    private class Position implements Positionable
    {
        private int row;
        private int col;
        
        public Position( int row, int col )
        {
            this.row = row;
            this.col = col;
        }
        
        public int getRow()
        { return row; }
        
        public int getColumn()
        { return col; }
        
        public char getValue()
        { return grid[ row ][ col ]; }
        
        public String toString()
        { return "(" + row + " , " + col + ")"; }
        
        /**
         * 
         * @return All the neighbors (in any direction) of a certain position
         */
        public List<Positionable> getNeighbors( )
        {
            int lowRow = ( row == 0 ) ? 0 : ( row - 1 );
            int lowCol = ( col == 0 ) ? 0 : ( col - 1 );
            int highRow = ( row == numRows - 1 ) ? row : ( row + 1 );
            int highCol = ( col == numCols - 1 ) ? col : ( col + 1 );

            List<Positionable> neighbors = new ArrayList<>( );

            // Iterate through the neighbors of this position and add them to the list
            for( int r = lowRow; r <= highRow; ++r )
                for( int c = lowCol; c <= highCol; ++c )
                    if( r != row || c != col )
                        neighbors.add( newPosition( r, c ) );

            return neighbors;
        }
        
        public boolean equals( Object other )
        {
            // Make sure that the paramter is an instance of Position before casting
            if( ! ( other instanceof Position ) )
                return false;
            
            Position otherPos = ( Position ) other;
            
            // Returns true iff both Position objects have the same row and column
            return row == otherPos.getRow( ) && col == otherPos.getColumn();
        }
        
        /**
         * The hashCode method is implemented since equals() was overridden.
         * This ensures that equal methods will have the same hashCode.
         * @return hashCode of the object
         */
        public int hashCode()
        {
            int hash = 7;
            hash = 59 * hash + this.row;
            hash = 59 * hash + this.col;
            return hash;
        }
    }
    
    /*** End of inner Position class ***/
    
    
    /*** Start of Puzzle class **/
    
    private static String [ ] dictionary;
    // Point scale used to determine the point value of a word according to its length
    private static final int [ ] pointScale = { 0, 0, 0, 1, 2, 3, 4, 6, 10, 15 };
    
    private char [ ][ ] grid;
    private int numRows;
    private int numCols;
    

    
    public Puzzle( String filename )
    {
        /*
         * Make sure that the dictionary list is initialized as soon as a Puzzle
         * object is created. The dictionary list is static so it only has to be
         * initialized once. The list is also sorted if it was not in the first place.
        */
        if ( dictionary == null )
        {
            boolean isSorted = readDictionary( "dict.txt" );
            
            if ( ! isSorted )
                Arrays.sort( dictionary );
        }
        
        grid = readPuzzle( filename );
        numRows = grid.length;
        numCols = grid[ 0 ].length;
    }
    
    /**
     * Read the dictionary, assign it to a static variable and determine whether 
     * it is sorted or not.
     * @param filename The name of the file containing the dictionary
     * @return True if the dictionary list is sorted, false otherwise
     */
    private static boolean readDictionary( String filename )
    {
        File file = new File( filename );
        List<String> dictList = new ArrayList<>( ); 
        boolean isSorted = true;
        
        BufferedReader br = null;
        
        try 
        {
            br = new BufferedReader( new FileReader( file ) );
            
            String line;
            String prevLine = "";
            while( ( line = br.readLine() ) != null )
            {
                line = line.trim();

                /* 
                 * Check whether the current word is lexicographically higher that 
                 * the previous word. If not, then the dictionary is not sorted
                */
                if ( isSorted && line.compareTo( prevLine ) < 0 )
                    isSorted = false;
                
                
                dictList.add( line );
                prevLine = line;
            }
            
        }
        catch( FileNotFoundException ex )
        {
            System.err.println( "Error: The file " + filename + " was not found." );
            System.exit( 1 );
        }
        catch( IOException ex )
        {
            System.err.println( "Error: An error ocurred while reading " + filename );
            System.exit( 1 );
        }
        finally 
        {
            try {
                if ( br != null )  
                    br.close();
            } 
            catch ( IOException ex ) {
                System.err.println( "An error ocurred when closing the stream for "
                                    + "the dictionary file." );
                System.exit( 1 );
            }
        }

        
        String [ ] dict = new String [ dictList.size( ) ];
        
        // Convert the dictionary list to a String array and assign it to the static variable
        dictionary = dictList.toArray( dict );
        
        return isSorted;
    }

    
    /*
     * Reads a n x m boggle puzzle while ensuring that it is properly formatted.
     * @param filename The name of the file containing the puzzle
     * @return The 2d char array containing the puzzle
    */
    private char [ ][ ] readPuzzle( String filename )
    {
        // Get the file containing the puzzle
        File file = new File( filename );
        // List that will store the rows of the puzzle
        List<String> rows = new ArrayList<>();
        Scanner scanner = null;
        
        try {
            scanner = new Scanner( file );
            
            /* The code below does not only read the puzzle file but ensures that the data
             * being read is properly formatted and the puzzle has appropiate dimensions
            */
            
            // Holds the number of columns that the previous row had
            int numColumns = 0;
            while ( scanner.hasNext() )
            {
                String row = scanner.next();
                
                // Initialize the numColumns variable ( this will occur when the first row is being read )
                if ( numColumns == 0 )
                    numColumns = row.length();
                
                /*
                 * If the length of this row is equal to the number of columns of 
                 * the previous row, then proceed
                */
                if ( row.length() == numColumns )
                    rows.add( row );
                // Otherwise, the puzzle has rows with different columns and is not properly formatted
                else
                    throw new IllegalStateException( "The puzzle given is not properly formatted: "
                            + "The rows do not have the same number of characters." );
                
                numColumns = row.length();
            }
            
            // If the puzzle file is empty, throw an exception
            if ( rows.isEmpty() )
                throw new IllegalStateException( "The puzzle given is not properly formatted: "
                            + "The puzzle file is empty." );
        }
        catch( FileNotFoundException ex ) {
            System.err.println( "Error: The file " + filename + " was not found." );
            System.exit( 1 );
        }
        catch( IllegalStateException ex )
        {
            System.err.println( ex.getMessage() );
            System.exit( 1 );
        }
        finally
        {
            // Close the scanner stream after use
            if ( scanner != null )
                scanner.close();
        }
        
        // Set up the 2d char array with the appropiate size
        char [ ][ ] puzzleGrid = new char[ rows.size() ][ rows.get( 0 ).length() ];
        
        /*
         * Iterate through the rows list, converting the row string into a char 
         * array and assigning it to the 2d char array.
        */
        int i = 0;
        for( String row : rows )
        {
            puzzleGrid[ i++ ] = row.toCharArray();
        }

        return puzzleGrid;
    }
    
    /**
     * Returns an object that implements Positionable at position (row,col)
     * @param row Row position  
     * @param col Column position
     * @return object that implements Positionable at position (row,col)
     */
    public Positionable newPosition( int row, int col )
    {
        return new Position( row, col );
    }
    
    /**
     * Routine to solve the Boggle game.
     * @return a Map containing words as keys, and the positions used
     *          to form the words (as a List) as values.
     */
    public Map<String,List<Positionable>> solve( )
    {
        // TreeMap that will hold the words along with their positions
        Map<String,List<Positionable>> results = new TreeMap<>( );
        List<Positionable> path = new ArrayList<>( );

        /*
         * Iterate through each of the characters in the grid and find all the 
         * words in the puzzle that with that character.
        */
        for( int row = 0; row < numRows; ++row )
            for( int col = 0; col < numCols; ++col )
            {
                solve( newPosition( row, col ), "", path, results );
                
                /*
                 * Clear any Position objects that was left in the path list
                 * because the next solve(...) call will be starting from a different letter
                */
                path.clear(); 
            }

        return results;
    }
    
    /**
     * Hidden recursive routine 
     * @param thisPos the current position
     * @param charSequence the characters in the potential matching string thus far
     * @param path the List of positions used to form the potential matching string thus far
     * @param results the Map that contains the words that have been found as keys
     *                 and the positions used to form the string (as a List) as values.
     */
    private void solve( Positionable thisPos, String charSequence, List<Positionable> path, Map<String,List<Positionable>> results )
    {
        /*
         * Since Boggle does not allow using the same grid character twice,
         * if the path already contains the current position, we exit.
        */
        if ( path.contains( thisPos ) )
            return;
            
        // Append the current character to the potential word being analyzed
        charSequence += thisPos.getValue();
        
        /*
         * binarySearch from Arrays.util will return the following:
         *  1) If the String is found in the dictionary, it will return a non-negative
         *  number representing the index where the word is.
         *  2) Otherwise, it returns (-(insertion point) - 1). The insertion point 
         *  is defined as the point at which the char sequence would be inserted into the array.
        */
        int index = Arrays.binarySearch( dictionary , charSequence );
        
        // If the index is not negative then the word was found
        if ( index >= 0 )
        {
            path.add( thisPos );
            
            /*
             * We make a copy of the path object because otherwise we would be 
             * storing a reference to it but it will be empty at the end of the program.
            */
            List<Positionable> copyPath = new ArrayList<>( path );
            
            // Put the found word into the results map along with its path list
            results.put( charSequence, copyPath );
        }
        
        /*
         * If the index is negative, then the word was not found. Now, we must check
         * whether the current char sequence that we have matches the prefix of
         * any word in the dictionary. Otherwise, it is pointless to continue.
        */
        else if ( index < 0 )
        {
            /*
             * The insertion point also represents the index of the word in the 
             * dictionary  that would be above (i.e. higher lexicographical order) 
             * than the current char sequence.
            */
            int insertionPoint = Math.abs( index ) - 1;
            
            /*
             * The only word which the current char sequence can be the prefix of
             * is the word in the insertion point index in the dictionary.
             * If it's not a prefix to this word then there is no point continuing with it
            */
            if ( ! isPrefix( charSequence, dictionary[ insertionPoint ] ) )
                return;
            
            
            path.add( thisPos );
        }
        
        // Iterate through the neighbors of the current Position we are now
        for( Positionable pos : thisPos.getNeighbors( ) )
        {
            solve( pos, charSequence, path, results );
            
            /*
             * Remove the extra Position objects in the path list that was left from
             * the previous solve call.
            */
            for( int i = path.size()-1; i >= charSequence.length(); --i )
                path.remove( i );
        }
    }
    
    /*
     * Determines whether the first paramater is a prefix of the second
     * @param prefix possible prefix
     * @param word string which we are analyzing for the prefix
    */
    public boolean isPrefix( String prefix, String word )
    {
        int length = 0;
        
        while ( length < prefix.length() && length < word.length() && 
                prefix.charAt( length ) == word.charAt( length ) )
            ++length;
        
        return length == prefix.length();
    }
    
    /**
     * Print the results of the solve() routine in a formatted way
     * @param results The Map containing the words and the list of Positions
     */
    public void printResults( Map<String,List<Positionable>> results )
    {
        int totalPoints = 0;
        System.out.println( "Total number of words found: " + results.keySet().size() );
        
        if ( results.size() > 200 )
        {
            System.out.println( "Since more than 200 words were found, only those of length 8 or bigger"
                                + " will be shown along with a summary of the rest of the words."); 
            
            // Store the number of words of length 3 to 7
            int [] wordCount = { 0,0,0,0,0,0,0,0 };
            
            System.out.printf( "%-10s%-6s  %s \n", "Words", "Points", "Positions" );
            System.out.println( "--------------------------------------------------" );
            
            for (Map.Entry<String,List<Positionable>> entry: results.entrySet()) {
                    String word = entry.getKey();
                    String positions =  entry.getValue().toString();
                    // Remove the brackets from the list of Positions string
                    positions = positions.substring( 1, positions.length() - 1);
                    // Get the point for that word.
                    int pointVal = pointScale[ Math.min( word.length(), 9 ) ];
                    
                    // If the word is 8 characters or longer print it
                    if ( word.length() >= 8 )
                    {
                        System.out.printf( "%-9s  %1d     %s \n", word, pointVal, positions );
                        totalPoints += pointVal;
                    }
                    // Otherwise update the count summary
                    else
                    {
                        wordCount[ word.length() ]++;
                        totalPoints += pointVal;
                    }
            }
            
            // Print the summary for words of length between 3 and 7
            for ( int i = 3; i < wordCount.length; ++i )
            {
                int numWords = wordCount[ i ];
                System.out.println( "Words of length " + i + ": " + numWords
                         + " account for " + numWords * pointScale[ i ]);
            }
        }
        // If <= 200 words were found, print them all
        else 
        {
            System.out.printf( "%-9s %-6s    %s \n", "Words", "Points", "Positions" );
            System.out.println( "--------------------------------------------------" );

            for (Map.Entry<String,List<Positionable>> entry: results.entrySet()) {
                    String word = entry.getKey();
                    String positions =  entry.getValue().toString();
                    // Remove the brackets from the list of Positions string
                    positions = positions.substring( 1, positions.length() - 1);
                    int pointVal = pointScale[ Math.min( word.length(), 9 ) ];

                    // Only print words of length 3 or bigger
                    if ( word.length() >= 3 )
                        System.out.printf( "%-9s  %1d     %s \n", word, pointVal, positions );

                    totalPoints += pointVal;
            }
        }
            
        System.out.println( "The total number of points is: " + totalPoints );
    }
}

public class BoggleSolver
{

    public static void main( String[ ] args )
    {
        // Ensure that the name of the file containing the puzzle is passed as a paramater
        if ( args.length != 1 )
        {
            System.err.println( "Error: the name of the file containing the puzzle "
                    + "must be passed as a parameter." );
            System.exit( 1 );
        }

        Puzzle puzzle = new Puzzle( args[ 0 ] );
        
        long start = System.currentTimeMillis();
        
        Map<String,List<Positionable>> results = puzzle.solve();
        
        long end = System.currentTimeMillis();
        
        puzzle.printResults( results );
        
        System.out.println( "It took " + ( end - start) + " ms to find the solution." );
        
    }
    
}
