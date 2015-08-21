/** Finds the longest common substring between two files. To do so, a suffix array
 * is constructed and then the longest common prefix array is calculated.
 * @author Alberto Mizrahi
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class LongestCommonSubstring
{
    
    /* 
     * The length of the string content of the second file. This is used in 
     * order to determine whether the suffix being analyzed is from the first or
     * the second file. i.e. Let n denote this length. Then if the suffix is
     * larger than n + 1, then the suffix is from the first file; otherwise
     * its from the second file.
    */
    private static int lengthSecondFile;

    /**
     * @param args The name of the two files that will be analyzed
     */
    public static void main(String[] args) 
    {
        // Ensure that only two file names as passed as paramaters of the program
        if ( args.length != 2 ) 
        {
            System.out.println( "The two files to be read must be passed as parameters." );
            System.exit( 1 );
        }

        File file1 = new File( args[ 0 ] );
        File file2 = new File( args[ 1 ] );

        String fileContents1 = readFile( file1 );
        String fileContents2 = readFile( file2 );
        lengthSecondFile = fileContents2.length();

        /*
         * The contents of the two files are concatenated together with a
         * delimeter separating them. StringBuilder is used for efficiency
        */
        StringBuilder sb = new StringBuilder( fileContents1 );
        sb.append( "˦" );   // Add delimeter
        sb.append( fileContents2 );
        EfficientString s = new EfficientString( sb.toString() );
        
        long start = System.currentTimeMillis();
        
        // Contains all the suffixes of the two files
        EfficientString [ ] suffixes = new EfficientString[ s.length() ];
        // Contains the longest common prefix for each suffix
        int [ ] LCP = new int[ s.length() ];

        // Find all the suffixes of the string
        for ( int i = 0; i < s.length(); ++i ) 
        {
            suffixes[ i ] = s.substring( i );
            //System.out.println(suffixes[ i ]);
        }
        
        Arrays.sort( suffixes );

        // Calculating the LCP for each pair of suffixes
        for ( int i = 1; i < LCP.length; ++i ) 
        {
            LCP[ i ] = longestCommonPrefix( suffixes[ i - 1 ], suffixes[ i ] );
        }

        // Find the index of the maximum LCP
        int maxLCPIndex = 0;
        for ( int i = 1; i < LCP.length; ++i ) 
        {
            if ( LCP[ i ] > LCP[ maxLCPIndex ] ) 
            {
                maxLCPIndex = i;
            }
        }

        // The length of the longest LCP
        int maxLCP = LCP[ maxLCPIndex ];
        // The longest common prefix
        EfficientString sLCP = suffixes[ maxLCPIndex ].substring( 0, maxLCP );
        
        long end = System.currentTimeMillis();
        
        System.out.println( "The longest common substring is " + maxLCP + 
                " characters: \n'" + displayLCP( sLCP ) + "'" );
        System.out.println( "It took " + ( end - start ) + " ms to find the answer.");

    }
    
    /**
     * Accepts a file and returns a string containing the contents of the
     * file. Since the Scanner collapses the whitespaces in between the words, 
     * a space is added in between the tokens.
     * @param file The file to be read
     * @return string containing the contents of the file
     */
    public static String readFile( File file ) 
    {
        StringBuilder sb = new StringBuilder();
        Scanner scanner = null;
        try 
        {
            scanner = new Scanner( file );
            while( scanner.hasNext() )
            {
                sb.append( scanner.next() ); // Append word to overall string
                sb.append( " " ); // Append a space to separate words
            }
            sb.deleteCharAt( sb.length() - 1 ); // Remove the extra space at the end
        } 
        catch ( FileNotFoundException ex ) 
        {
            System.out.println( "The file " + file + " was not found." );
            System.exit( 1 );
        } 
        finally
        {
            if ( scanner != null )
                scanner.close();    // Safely close the Scanner after use
        }
        
       return sb.toString();
    }

    /**
     * Calculates the length of the LCP between two strings
     * @param s1
     * @param s2
     * @return the length of the LCP between the two strings
     */
    public static int longestCommonPrefix( EfficientString s1, EfficientString s2 ) 
    {
        // Length of the longest common prefix of the two string
        int length = 0; 
        
        /*
         * 1) If the lengths of the two strings being analyzed are less than
         * the length of second file, then these two strings come from the 
         * second file.
         * 2) If the lengths of the two strings being analyzed are bigger than
         * the length of the second file plus one ( the delimeter ), then both
         * strings come from the first file.
         * In both cases, we don't care about them since we are trying to find
         * the longest substring common to both files
        */
        if ( (s1.length < lengthSecondFile && s2.length < lengthSecondFile) ||
             (s1.length > lengthSecondFile+1 && s2.length > lengthSecondFile+1) )
            return 0;

        // Determine the length of the LCP
        while ( length < s1.length() && length < s2.length()
               && s1.charAt( length ) == s2.charAt( length ) ) 
        {
            ++length;
        }

        return length;
    }
    
    /**
     * Returns the whole LCP is if its less than 30 characters long
     * or returns the first 30 characters 
     * @param LCP longest common prefix
     * @return 
     */
    public static String displayLCP( EfficientString LCP )
    { return LCP.toString( ); }

}

class EfficientString implements Comparable<EfficientString> 
{

    public String source;
    public int startIndex;
    public int length;

    public EfficientString( String source ) 
    {
        this.source = source;
        startIndex = 0;
        length = source.length();
    }

    public EfficientString( String source, int startIndex, int length ) 
    {
        this.source = source;
        this.startIndex = startIndex;
        this.length = length;
    }

    public int length() 
    {
        return length;
    }

    public char charAt( int index ) 
    {
        if ( index < 0 || index >= length ) 
        {
            throw new StringIndexOutOfBoundsException();
        }

        return source.charAt( startIndex + index );
    }

    public EfficientString substring( int startIdx ) 
    {
        return substring( startIdx, length );
    }

    public EfficientString substring( int startIdx, int endIdx ) 
    {
        return new EfficientString( source, startIndex + startIdx, endIdx - startIdx );
    }
    
    public String toString() 
    {
        return source.substring( startIndex, startIndex + length );
    }

    public int compareTo(EfficientString rhs) 
    {
        // Store the necessary info of this EfficientString object
        int lhsStartIndex = startIndex;
        int lhsEndIndex = startIndex + length;
        String lhsString = source;
        
        // Store the necessary info of the EfficientString object passed
        int rhsStartIndex = rhs.startIndex;
        int rhsEndIndex = rhs.startIndex + rhs.length;
        String rhsString = rhs.source;
        
        while ( lhsStartIndex < lhsEndIndex && 
                rhsStartIndex < rhsEndIndex &&
                lhsString.charAt( lhsStartIndex ) == rhsString.charAt( rhsStartIndex ) )
        {
            ++lhsStartIndex;
            ++rhsStartIndex;
        }
        
        
        if ( lhsStartIndex == lhsEndIndex ) // If we traversed the entire left string
        {
            if ( rhsStartIndex == rhsEndIndex )   // If we traversed the entire right string
                return 0;   // Then both stirng are obviously the same
            else
                return -1; // Otherwise, the left string is shorter so it comes before the right string
        }
        
        else if ( rhsStartIndex == rhsEndIndex ) // If we traversed the entire right string (and not the left string)
            return 1; // Then the right string obviously comes before the left string
        
        /* 
         * Otherwise we substract the ASCII values of the differing characters
         * between the left and right string. If the character of the LHS comes
         * before the character of the RHS, the substraction will result in a 
         * negative number correctly implying that LHS comes before RHS. Else,
         * the opposite will happen.
        */
        else
            return lhsString.charAt( lhsStartIndex ) - rhsString.charAt( rhsStartIndex );
        
    }
}
