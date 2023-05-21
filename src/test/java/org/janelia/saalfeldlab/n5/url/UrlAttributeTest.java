package org.janelia.saalfeldlab.n5.url;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.janelia.saalfeldlab.n5.N5FSWriter;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UrlAttributeTest
{
	N5Reader n5;

	String rootContext = "";

	int[] list;

	HashMap< String, String > obj;

	Set< String > rootKeys;

	TestInts testObjInts;

	TestDoubles testObjDoubles;

	@BeforeEach
	public void before()
	{
		try
		{
			n5 = new N5FSWriter( "src/test/resources/url/urlAttributes.n5" );
			rootContext = "";
			list = new int[] { 0, 1, 2, 3 };

			obj = new HashMap<>();
			obj.put( "a", "aa" );
			obj.put( "b", "bb" );
			rootKeys = new HashSet<>();
			rootKeys.addAll( Stream.of( "n5", "foo", "list", "object" ).collect( Collectors.toList() ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		testObjInts = new TestInts( "ints", "intsName", new int[] { 5, 4, 3 } );
		testObjDoubles = new TestDoubles( "doubles", "doublesName", new double[] { 5.5, 4.4, 3.3 } );
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testRootAttributes() throws URISyntaxException, IOException
	{
		// get
		Map< String, Object > everything = getAttribute( n5, new N5URL( "" ), Map.class );
		final String version = N5Reader.VERSION.toString();
		assertEquals( version, everything.get( "n5" ), "empty url" );

		Map< String, Object > everything2 = getAttribute( n5, new N5URL( "#/" ), Map.class );
		assertEquals( version, everything2.get( "n5" ), "root attribute" );

		assertEquals( "bar", getAttribute( n5, new N5URL( "#foo" ), String.class ), "url to attribute" );
		assertEquals( "bar", getAttribute( n5, new N5URL( "#/foo" ), String.class ), "url to attribute absolute" );

		assertEquals( "bar", getAttribute( n5, new N5URL( "#foo" ), String.class ), "#foo" );
		assertEquals( "bar", getAttribute( n5, new N5URL( "#/foo" ), String.class ), "#/foo" );
		assertEquals( "bar", getAttribute( n5, new N5URL( "?#foo" ), String.class ), "?#foo" );
		assertEquals( "bar", getAttribute( n5, new N5URL( "?#/foo" ), String.class ), "?#/foo" );
		assertEquals( "bar", getAttribute( n5, new N5URL( "?/#/foo" ), String.class ), "?/#/foo" );
		assertEquals( "bar", getAttribute( n5, new N5URL( "?/.#/foo" ), String.class ), "?/.#/foo" );
		assertEquals( "bar", getAttribute( n5, new N5URL( "?./#/foo" ), String.class ), "?./#/foo" );
		assertEquals( "bar", getAttribute( n5, new N5URL( "?.#foo" ), String.class ), "?.#foo" );
		assertEquals( "bar", getAttribute( n5, new N5URL( "?/a/..#foo" ), String.class ), "?/a/..#foo" );
		assertEquals( "bar", getAttribute( n5, new N5URL( "?/a/../.#foo" ), String.class ), "?/a/../.#foo" );

		/* whitespace-encoding-necesary, fragment-only test*/
		assertEquals( "b a r", getAttribute( n5, new N5URL( "#f o o" ), String.class ), "#f o o" );

		assertArrayEquals( list, getAttribute( n5, new N5URL( "#list" ), int[].class ), "url list" );

		// list
		assertEquals( list[ 0 ], ( int ) getAttribute( n5, new N5URL( "#list[0]" ), Integer.class ), "url list[0]" );
		assertEquals( list[ 1 ], ( int ) getAttribute( n5, new N5URL( "#list[1]" ), Integer.class ), "url list[1]" );
		assertEquals( list[ 2 ], ( int ) getAttribute( n5, new N5URL( "#list[2]" ), Integer.class ), "url list[2]" );

		assertEquals( list[ 3 ], ( int ) getAttribute( n5, new N5URL( "#list[3]" ), Integer.class ), "url list[3]" );
		assertEquals( list[ 3 ], ( int ) getAttribute( n5, new N5URL( "#list/[3]" ), Integer.class ), "url list/[3]" );
		assertEquals( list[ 3 ], ( int ) getAttribute( n5, new N5URL( "#list//[3]" ), Integer.class ), "url list//[3]" );
		assertEquals( list[ 3 ], ( int ) getAttribute( n5, new N5URL( "#//list//[3]" ), Integer.class ), "url //list//[3]" );
		assertEquals( list[ 3 ], ( int ) getAttribute( n5, new N5URL( "#//list////[3]//" ), Integer.class ), "url //list//[3]//" );

		// object
		assertTrue( mapsEqual( obj, getAttribute( n5, new N5URL( "#object" ), Map.class ) ), "url object" );
		assertEquals( "aa", getAttribute( n5, new N5URL( "#object/a" ), String.class ), "url object/a" );
		assertEquals( "bb", getAttribute( n5, new N5URL( "#object/b" ), String.class ), "url object/b" );
	}

	@Test
	public void testPathAttributes() throws URISyntaxException, IOException
	{

		final String a = "a";
		final String aa = "aa";
		final String aaa = "aaa";

		final N5URL aUrl = new N5URL( "?/a" );
		final N5URL aaUrl = new N5URL( "?/a/aa" );
		final N5URL aaaUrl = new N5URL( "?/a/aa/aaa" );

		// name of a
		assertEquals( a, getAttribute( n5, "?/a#name", String.class ), "name of a from root" );
		assertEquals( a, getAttribute( n5, new N5URL( "?a#name" ), String.class ), "name of a from root" );
		assertEquals( a, getAttribute( n5, aUrl.resolve( new N5URL( "?/a#name" ) ), String.class ), "name of a from a" );
		assertEquals( a, getAttribute( n5, aaUrl.resolve( "?..#name" ), String.class ), "name of a from aa" );
		assertEquals( a, getAttribute( n5, aaaUrl.resolve( new N5URL( "?../..#name" ) ), String.class ), "name of a from aaa" );

		// name of aa
		assertEquals( aa, getAttribute( n5, new N5URL( "?/a/aa#name" ), String.class ), "name of aa from root" );
		assertEquals( aa, getAttribute( n5, new N5URL( "?a/aa#name" ), String.class ), "name of aa from root" );
		assertEquals( aa, getAttribute( n5, aUrl.resolve( "?aa#name" ), String.class ), "name of aa from a" );

		assertEquals( aa, getAttribute( n5, aaUrl.resolve( "?./#name" ), String.class ), "name of aa from aa" );

		assertEquals( aa, getAttribute( n5, aaUrl.resolve( "#name" ), String.class ), "name of aa from aa" );
		assertEquals( aa, getAttribute( n5, aaaUrl.resolve( "?..#name" ), String.class ), "name of aa from aaa" );

		// name of aaa
		assertEquals( aaa, getAttribute( n5, new N5URL( "?/a/aa/aaa#name" ), String.class ), "name of aaa from root" );
		assertEquals( aaa, getAttribute( n5, new N5URL( "?a/aa/aaa#name" ), String.class ), "name of aaa from root" );
		assertEquals( aaa, getAttribute( n5, aUrl.resolve( "?aa/aaa#name" ), String.class ), "name of aaa from a" );
		assertEquals( aaa, getAttribute( n5, aaUrl.resolve( "?aaa#name" ), String.class ), "name of aaa from aa" );
		assertEquals( aaa, getAttribute( n5, aaaUrl.resolve( "#name" ), String.class ), "name of aaa from aaa" );

		assertEquals( aaa, getAttribute( n5, aaaUrl.resolve( "?./#name" ), String.class ), "name of aaa from aaa" );

		assertEquals( (Integer)1, getAttribute( n5, new N5URL( "#nestedList[0][0][0]" ), Integer.class ), "nested list 1" );
		assertEquals( (Integer)1, getAttribute( n5, new N5URL( "#/nestedList/[0][0][0]" ), Integer.class ), "nested list 1" );
		assertEquals( (Integer)1, getAttribute( n5, new N5URL( "#nestedList//[0]/[0]///[0]" ), Integer.class ), "nested list 1" );
		assertEquals( (Integer)1, getAttribute( n5, new N5URL( "#/nestedList[0]//[0][0]" ), Integer.class ), "nested list 1" );

	}

	private < T > T getAttribute( final N5Reader n5, final String url1, Class< T > clazz ) throws URISyntaxException, IOException
	{
		return getAttribute( n5, url1, null, clazz );
	}

	private < T > T getAttribute( final N5Reader n5, final String url1, final String url2, Class< T > clazz ) throws URISyntaxException, IOException
	{
		final N5URL n5URL = url2 == null ? new N5URL( url1 ) : new N5URL( url1 ).resolve( url2 );
		return getAttribute( n5, n5URL, clazz );
	}

	private < T > T getAttribute( final N5Reader n5, final N5URL url1, Class< T > clazz ) throws URISyntaxException, IOException
	{
		return getAttribute( n5, url1, null, clazz );
	}

	private < T > T getAttribute( final N5Reader n5, final N5URL url1, final String url2, Class< T > clazz ) throws URISyntaxException, IOException
	{
		final N5URL n5URL = url2 == null ? url1 : url1.resolve( url2 );
		return n5.getAttribute( n5URL.getGroupPath(), n5URL.getAttributePath(), clazz );
	}

	@Test
	public void testPathObject() throws IOException, URISyntaxException
	{
		final TestInts ints = getAttribute( n5, new N5URL( "?objs#intsKey" ), TestInts.class );
		assertEquals( testObjInts.name, ints.name );
		assertEquals( testObjInts.type, ints.type );
		assertArrayEquals( testObjInts.t(), ints.t() );

		final TestDoubles doubles = getAttribute( n5, new N5URL( "?objs#doublesKey" ), TestDoubles.class );
		assertEquals( testObjDoubles.name, doubles.name );
		assertEquals( testObjDoubles.type, doubles.type );
		assertArrayEquals( testObjDoubles.t(), doubles.t(), 1e-9 );

		final TestDoubles[] doubleArray = getAttribute( n5, new N5URL( "?objs#array" ), TestDoubles[].class );
		final TestDoubles doubles1 = new TestDoubles( "doubles", "doubles1", new double[] { 5.7, 4.5, 3.4 } );
		final TestDoubles doubles2 = new TestDoubles( "doubles", "doubles2", new double[] { 5.8, 4.6, 3.5 } );
		final TestDoubles doubles3 = new TestDoubles( "doubles", "doubles3", new double[] { 5.9, 4.7, 3.6 } );
		final TestDoubles doubles4 = new TestDoubles( "doubles", "doubles4", new double[] { 5.10, 4.8, 3.7 } );
		final TestDoubles[] expectedDoubles = new TestDoubles[] { doubles1, doubles2, doubles3, doubles4 };
		assertArrayEquals( expectedDoubles, doubleArray );

		final String[] stringArray = getAttribute( n5, new N5URL( "?objs#String" ), String[].class );
		final String[] expectedString = new String[] { "This", "is", "a", "test" };

		final Integer[] integerArray = getAttribute( n5, new N5URL( "?objs#Integer" ), Integer[].class );
		final Integer[] expectedInteger = new Integer[] { 1, 2, 3, 4 };

		final int[] intArray = getAttribute( n5, new N5URL( "?objs#int" ), int[].class );
		final int[] expectedInt = new int[] { 1, 2, 3, 4 };
		assertArrayEquals( expectedInt, intArray );
	}

	private < K, V > boolean mapsEqual( Map< K, V > a, Map< K, V > b )
	{
		if ( !a.keySet().equals( b.keySet() ) )
			return false;

		for ( K k : a.keySet() )
		{
			if ( !a.get( k ).equals( b.get( k ) ) )
				return false;
		}

		return true;
	}

	private static class TestObject< T >
	{
		String type;

		String name;

		T t;

		public TestObject( String type, String name, T t )
		{
			this.name = name;
			this.type = type;
			this.t = t;
		}

		public T t()
		{
			return t;
		}

		@Override
		public boolean equals( Object obj )
		{

			if ( obj.getClass() == this.getClass() )
			{
				final TestObject< ? > otherTestObject = ( TestObject< ? > ) obj;
				return Objects.equals( name, otherTestObject.name )
						&& Objects.equals( type, otherTestObject.type )
						&& Objects.equals( t, otherTestObject.t );
			}
			return false;
		}
	}

	public static class TestDoubles extends TestObject< double[] >
	{
		public TestDoubles( String type, String name, double[] t )
		{
			super( type, name, t );
		}

		@Override
		public boolean equals( Object obj )
		{

			if ( obj.getClass() == this.getClass() )
			{
				final TestDoubles otherTestObject = ( TestDoubles ) obj;
				return Objects.equals( name, otherTestObject.name )
						&& Objects.equals( type, otherTestObject.type )
						&& Arrays.equals( t, otherTestObject.t );
			}
			return false;
		}
	}

	private static class TestInts extends TestObject< int[] >
	{
		public TestInts( String type, String name, int[] t )
		{
			super( type, name, t );
		}

		@Override
		public boolean equals( Object obj )
		{

			if ( obj.getClass() == this.getClass() )
			{
				final TestInts otherTestObject = ( TestInts ) obj;
				return Objects.equals( name, otherTestObject.name )
						&& Objects.equals( type, otherTestObject.type )
						&& Arrays.equals( t, otherTestObject.t );
			}
			return false;
		}

	}

}
