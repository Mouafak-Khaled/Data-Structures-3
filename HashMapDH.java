package code;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import given.AbstractHashMap;
import given.HashEntry;
import given.iPrintable;

/*
 * The file should contain the implementation of a hashmap with:
 * - Open addressing for collision handling
 * - Double hashing for probing. The double hash function should be of the form: q - (k mod q)
 * - Multiply-Add-Divide (MAD) for compression: (a*k+b) mod p
 * - Resizing (to double its size) and rehashing when the load factor gets above a threshold
 * 
 * Some helper functions are provided to you. We suggest that you go over them.
 * 
 * You are not allowed to use any existing java data structures other than for the keyset method
 */

public class HashMapDH<Key, Value> extends AbstractHashMap<Key, Value> {

	// The underlying array to hold hash entries (see the HashEntry class)
	private HashEntry<Key, Value>[] buckets;

	// Do not forget to call this when you need to increase the size!
	@SuppressWarnings("unchecked")
	protected void resizeBuckets(int newSize) {
		// Update the capacity
		N = nextPrime(newSize);
		buckets = (HashEntry<Key, Value>[]) Array.newInstance(HashEntry.class, N);
	}

	// The threshold of the load factor for resizing
	protected float criticalLoadFactor;

	// The prime number for the secondary hash
	int dhP;

	/*
	 * ADD MORE FIELDS IF NEEDED
	 * 
	 */

	/*
	 * ADD A NESTED CLASS IF NEEDED
	 * 
	 */

	// Default constructor
	public HashMapDH() {
		this(101);
	}

	public HashMapDH(int initSize) {
		this(initSize, 0.6f);
	}

	public HashMapDH(int initSize, float criticalAlpha) {
		N = initSize;
		criticalLoadFactor = criticalAlpha;
		resizeBuckets(N);

		// Set up the MAD compression and secondary hash parameters
		updateHashParams();

		/*
		 * ADD MORE CODE IF NEEDED
		 * 
		 */
	}

	/*
	 * ADD MORE METHODS IF NEEDED
	 * 
	 */

	/**
	 * Calculates the hash value by compressing the given hashcode. Note that you
	 * need to use the Multiple-Add-Divide method. The class variables "a" is the
	 * scale, "b" is the shift, "mainP" is the prime which are calculated for you.
	 * Do not include the size of the array here
	 * 
	 * Make sure to include the absolute value since there maybe integer overflow!
	 */
	protected int primaryHash(int hashCode) {
		// TODO: Implement MAD compression given the hash code, should be 1 line
		return Math.abs((a * Math.abs(hashCode) + b) % P) % N;
	}

	/**
	 * The secondary hash function. Remember you need to use "dhP" here!
	 * 
	 */
	protected int secondaryHash(int hashCode) {
		// TODO: Implement the secondary hash function taught in the class
		return dhP - Math.abs(hashCode) % dhP;
	}

	@Override
	public int hashValue(Key key, int iter) {
		int k = Math.abs(key.hashCode());
		return Math.abs(primaryHash(k) + iter * secondaryHash(k)) % N;
	}

	/**
	 * checkAndResize checks whether the current load factor is greater than the
	 * specified critical load factor. If it is, the table size should be increased
	 * to 2*N and recreate the hash table for the keys (rehashing). Do not forget to
	 * re-calculate the hash parameters and do not forget to re-populate the new
	 * array!
	 */
	protected void checkAndResize() {
		if (loadFactor() > criticalLoadFactor) {
			// TODO: Fill this yourself
			HashEntry<Key, Value>[] tmp = buckets;
			resizeBuckets(2 * N);
			updateHashParams();

			for (int i = 0, counter = 0; i < tmp.length; i++) {

				if (tmp[i] == null)
					continue;

				int h = hashValue(tmp[i].getKey(), counter);

				while (buckets[h] != null) {

					counter++;
					h = hashValue(tmp[i].getKey(), counter);

				}

				buckets[h] = tmp[i];
				counter = 0;

			}
		}

	}

	@Override
	public Value get(Key k) {
		// TODO Auto-generated method stub
		if (k == null)
			return null;

		int i = indexOf(k);
		if (i != -1)
			return buckets[i].getValue();

		return null;
	}

	public int indexOf(Key k) {

		for (int i = 0; i < buckets.length; i++) {

			if (buckets[i] == null)
				continue;

			if (k.equals(buckets[i].getKey())) {

				return i;
			}

		}

		return -1;

	}

	@Override
	public Value put(Key k, Value v) {
		// TODO Auto-generated method stub
		// Do not forget to resize if needed!

		if (k == null)
			return null;

		checkAndResize();
		int counter = 0;
		int h = hashValue(k, counter);
		int index = indexOf(k);

		if (index != -1) {

			Value val = buckets[index].getValue();
			buckets[index].setValue(v);
			return val;

		} else {

			while (true) {
				if (buckets[h] == null) {
					buckets[h] = new HashEntry<Key, Value>(k, v);
					n++;
					return null;
				}

				counter++;
				h = hashValue(k, counter);

			}
		}
	}

	@Override
	public Value remove(Key k) {
		// TODO Auto-generated method stub

		if (k == null)
			return null;

		int i = indexOf(k);
		if (i != -1) {
			Value val = buckets[i].getValue();
			buckets[i] = null;
			n--;
			return val;
		}

		return null;
	}

	// This is the only function you are allowed to use an existing Java data
	// structure!
	@Override
	public Iterable<Key> keySet() {
		// TODO Auto-generated method stub

		Set<Key> keyList = new HashSet<Key>();
		for (int i = 0; i < buckets.length; i++) {

			if (buckets[i] == null)
				continue;

			keyList.add(buckets[i].getKey());

		}
		return keyList;
	}

	@Override
	protected void updateHashParams() {
		super.updateHashParams();
		dhP = nextPrime(N / 2);
	}

}
