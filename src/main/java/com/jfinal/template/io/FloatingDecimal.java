/*
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */

package com.jfinal.template.io;

public class FloatingDecimal{
    boolean     isExceptional;
    boolean     isNegative;
    int         decExponent;
    char        digits[];
    int         nDigits;
    int         bigIntExp;
    int         bigIntNBits;
    boolean     mustSetRoundDir = false;
    boolean     fromHex = false;
    int         roundDir = 0; // set by doubleValue

    /*
     * Constants of the implementation
     * Most are IEEE-754 related.
     * (There are more really boring constants at the end.)
     */
    static final long   signMask = 0x8000000000000000L;
    static final long   expMask  = 0x7ff0000000000000L;
    static final long   fractMask= ~(signMask|expMask);
    static final int    expShift = 52;
    static final int    expBias  = 1023;
    static final long   fractHOB = ( 1L<<expShift ); // assumed High-Order bit
    static final long   expOne   = ((long)expBias)<<expShift; // exponent of 1.0
    static final int    maxSmallBinExp = 62;
    static final int    minSmallBinExp = -( 63 / 3 );
    static final int    maxDecimalDigits = 15;
    static final int    maxDecimalExponent = 308;
    static final int    minDecimalExponent = -324;
    static final int    bigDecimalExponent = 324; // i.e. abs(minDecimalExponent)

    static final long   highbyte = 0xff00000000000000L;
    static final long   highbit  = 0x8000000000000000L;
    static final long   lowbytes = ~highbyte;

    static final int    singleSignMask =    0x80000000;
    static final int    singleExpMask  =    0x7f800000;
    static final int    singleFractMask =   ~(singleSignMask|singleExpMask);
    static final int    singleExpShift  =   23;
    static final int    singleFractHOB  =   1<<singleExpShift;
    static final int    singleExpBias   =   127;
    static final int    singleMaxDecimalDigits = 7;
    static final int    singleMaxDecimalExponent = 38;
    static final int    singleMinDecimalExponent = -45;

    static final int    intDecimalDigits = 9;


    /*
     * count number of bits from high-order 1 bit to low-order 1 bit,
     * inclusive.
     */
    private static int
    countBits( long v ){
        //
        // the strategy is to shift until we get a non-zero sign bit
        // then shift until we have no bits left, counting the difference.
        // we do byte shifting as a hack. Hope it helps.
        //
        if ( v == 0L ) return 0;

        while ( ( v & highbyte ) == 0L ){
            v <<= 8;
        }
        while ( v > 0L ) { // i.e. while ((v&highbit) == 0L )
            v <<= 1;
        }

        int n = 0;
        while (( v & lowbytes ) != 0L ){
            v <<= 8;
            n += 8;
        }
        while ( v != 0L ){
            v <<= 1;
            n += 1;
        }
        return n;
    }

    /*
     * Keep big powers of 5 handy for future reference.
     */
    private static FDBigInt b5p[];

    private static synchronized FDBigInt
    big5pow( int p ){
        assert p >= 0 : p; // negative power of 5
        if ( b5p == null ){
            b5p = new FDBigInt[ p+1 ];
        }else if (b5p.length <= p ){
            FDBigInt t[] = new FDBigInt[ p+1 ];
            System.arraycopy( b5p, 0, t, 0, b5p.length );
            b5p = t;
        }
        if ( b5p[p] != null )
            return b5p[p];
        else if ( p < small5pow.length )
            return b5p[p] = new FDBigInt( small5pow[p] );
        else if ( p < long5pow.length )
            return b5p[p] = new FDBigInt( long5pow[p] );
        else {
            // construct the value.
            // recursively.
            int q, r;
            // in order to compute 5^p,
            // compute its square root, 5^(p/2) and square.
            // or, let q = p / 2, r = p -q, then
            // 5^p = 5^(q+r) = 5^q * 5^r
            q = p >> 1;
            r = p - q;
            FDBigInt bigq =  b5p[q];
            if ( bigq == null )
                bigq = big5pow ( q );
            if ( r < small5pow.length ){
                return (b5p[p] = bigq.mult( small5pow[r] ) );
            }else{
                FDBigInt bigr = b5p[ r ];
                if ( bigr == null )
                    bigr = big5pow( r );
                return (b5p[p] = bigq.mult( bigr ) );
            }
        }
    }

    //
    // a common operation
    //
    private static FDBigInt
    multPow52( FDBigInt v, int p5, int p2 ){
        if ( p5 != 0 ){
            if ( p5 < small5pow.length ){
                v = v.mult( small5pow[p5] );
            } else {
                v = v.mult( big5pow( p5 ) );
            }
        }
        if ( p2 != 0 ){
            v.lshiftMe( p2 );
        }
        return v;
    }

    //
    // another common operation
    //
    private static FDBigInt
    constructPow52( int p5, int p2 ){
        FDBigInt v = new FDBigInt( big5pow( p5 ) );
        if ( p2 != 0 ){
            v.lshiftMe( p2 );
        }
        return v;
    }

    /*
     * This is the easy subcase --
     * all the significant bits, after scaling, are held in lvalue.
     * negSign and decExponent tell us what processing and scaling
     * has already been done. Exceptional cases have already been
     * stripped out.
     * In particular:
     * lvalue is a finite number (not Inf, nor NaN)
     * lvalue > 0L (not zero, nor negative).
     *
     * The only reason that we develop the digits here, rather than
     * calling on Long.toString() is that we can do it a little faster,
     * and besides want to treat trailing 0s specially. If Long.toString
     * changes, we should re-evaluate this strategy!
     */
    private void
    developLongDigits( int decExponent, long lvalue, long insignificant ){
        char digits[];
        int  ndigits;
        int  digitno;
        int  c;
        //
        // Discard non-significant low-order bits, while rounding,
        // up to insignificant value.
        int i;
        for ( i = 0; insignificant >= 10L; i++ )
            insignificant /= 10L;
        if ( i != 0 ){
            long pow10 = long5pow[i] << i; // 10^i == 5^i * 2^i;
            long residue = lvalue % pow10;
            lvalue /= pow10;
            decExponent += i;
            if ( residue >= (pow10>>1) ){
                // round up based on the low-order bits we're discarding
                lvalue++;
            }
        }
        if ( lvalue <= Integer.MAX_VALUE ){
            assert lvalue > 0L : lvalue; // lvalue <= 0
            // even easier subcase!
            // can do int arithmetic rather than long!
            int  ivalue = (int)lvalue;
            ndigits = 10;
            digits = (char[])(perThreadBuffer.get());
            digitno = ndigits-1;
            c = ivalue%10;
            ivalue /= 10;
            while ( c == 0 ){
                decExponent++;
                c = ivalue%10;
                ivalue /= 10;
            }
            while ( ivalue != 0){
                digits[digitno--] = (char)(c+'0');
                decExponent++;
                c = ivalue%10;
                ivalue /= 10;
            }
            digits[digitno] = (char)(c+'0');
        } else {
            // same algorithm as above (same bugs, too )
            // but using long arithmetic.
            ndigits = 20;
            digits = (char[])(perThreadBuffer.get());
            digitno = ndigits-1;
            c = (int)(lvalue%10L);
            lvalue /= 10L;
            while ( c == 0 ){
                decExponent++;
                c = (int)(lvalue%10L);
                lvalue /= 10L;
            }
            while ( lvalue != 0L ){
                digits[digitno--] = (char)(c+'0');
                decExponent++;
                c = (int)(lvalue%10L);
                lvalue /= 10;
            }
            digits[digitno] = (char)(c+'0');
        }
        char result [];
        ndigits -= digitno;
        result = new char[ ndigits ];
        System.arraycopy( digits, digitno, result, 0, ndigits );
        this.digits = result;
        this.decExponent = decExponent+1;
        this.nDigits = ndigits;
    }

    //
    // add one to the least significant digit.
    // in the unlikely event there is a carry out,
    // deal with it.
    // assert that this will only happen where there
    // is only one digit, e.g. (float)1e-44 seems to do it.
    //
    private void
    roundup(){
        int i;
        int q = digits[ i = (nDigits-1)];
        if ( q == '9' ){
            while ( q == '9' && i > 0 ){
                digits[i] = '0';
                q = digits[--i];
            }
            if ( q == '9' ){
                // carryout! High-order 1, rest 0s, larger exp.
                decExponent += 1;
                digits[0] = '1';
                return;
            }
            // else fall through.
        }
        digits[i] = (char)(q+1);
    }

    /*
     * FIRST IMPORTANT CONSTRUCTOR: DOUBLE
     */
    public FloatingDecimal( double d )
    {
        long    dBits = Double.doubleToLongBits( d );
        long    fractBits;
        int     binExp;
        int     nSignificantBits;

        // discover and delete sign
        if ( (dBits&signMask) != 0 ){
            isNegative = true;
            dBits ^= signMask;
        } else {
            isNegative = false;
        }
        // Begin to unpack
        // Discover obvious special cases of NaN and Infinity.
        binExp = (int)( (dBits&expMask) >> expShift );
        fractBits = dBits&fractMask;
        if ( binExp == (int)(expMask>>expShift) ) {
            isExceptional = true;
            if ( fractBits == 0L ){
                digits =  infinity;
            } else {
                digits = notANumber;
                isNegative = false; // NaN has no sign!
            }
            nDigits = digits.length;
            return;
        }
        isExceptional = false;
        // Finish unpacking
        // Normalize denormalized numbers.
        // Insert assumed high-order bit for normalized numbers.
        // Subtract exponent bias.
        if ( binExp == 0 ){
            if ( fractBits == 0L ){
                // not a denorm, just a 0!
                decExponent = 0;
                digits = zero;
                nDigits = 1;
                return;
            }
            while ( (fractBits&fractHOB) == 0L ){
                fractBits <<= 1;
                binExp -= 1;
            }
            nSignificantBits = expShift + binExp +1; // recall binExp is  - shift count.
            binExp += 1;
        } else {
            fractBits |= fractHOB;
            nSignificantBits = expShift+1;
        }
        binExp -= expBias;
        // call the routine that actually does all the hard work.
        dtoa( binExp, fractBits, nSignificantBits );
    }

    /*
     * SECOND IMPORTANT CONSTRUCTOR: SINGLE
     */
    public FloatingDecimal( float f )
    {
        int     fBits = Float.floatToIntBits( f );
        int     fractBits;
        int     binExp;
        int     nSignificantBits;

        // discover and delete sign
        if ( (fBits&singleSignMask) != 0 ){
            isNegative = true;
            fBits ^= singleSignMask;
        } else {
            isNegative = false;
        }
        // Begin to unpack
        // Discover obvious special cases of NaN and Infinity.
        binExp = (int)( (fBits&singleExpMask) >> singleExpShift );
        fractBits = fBits&singleFractMask;
        if ( binExp == (int)(singleExpMask>>singleExpShift) ) {
            isExceptional = true;
            if ( fractBits == 0L ){
                digits =  infinity;
            } else {
                digits = notANumber;
                isNegative = false; // NaN has no sign!
            }
            nDigits = digits.length;
            return;
        }
        isExceptional = false;
        // Finish unpacking
        // Normalize denormalized numbers.
        // Insert assumed high-order bit for normalized numbers.
        // Subtract exponent bias.
        if ( binExp == 0 ){
            if ( fractBits == 0 ){
                // not a denorm, just a 0!
                decExponent = 0;
                digits = zero;
                nDigits = 1;
                return;
            }
            while ( (fractBits&singleFractHOB) == 0 ){
                fractBits <<= 1;
                binExp -= 1;
            }
            nSignificantBits = singleExpShift + binExp +1; // recall binExp is  - shift count.
            binExp += 1;
        } else {
            fractBits |= singleFractHOB;
            nSignificantBits = singleExpShift+1;
        }
        binExp -= singleExpBias;
        // call the routine that actually does all the hard work.
        dtoa( binExp, ((long)fractBits)<<(expShift-singleExpShift), nSignificantBits );
    }

    private void
    dtoa( int binExp, long fractBits, int nSignificantBits )
    {
        int     nFractBits; // number of significant bits of fractBits;
        int     nTinyBits;  // number of these to the right of the point.
        int     decExp;

        // Examine number. Determine if it is an easy case,
        // which we can do pretty trivially using float/long conversion,
        // or whether we must do real work.
        nFractBits = countBits( fractBits );
        nTinyBits = Math.max( 0, nFractBits - binExp - 1 );
        if ( binExp <= maxSmallBinExp && binExp >= minSmallBinExp ){
            // Look more closely at the number to decide if,
            // with scaling by 10^nTinyBits, the result will fit in
            // a long.
            if ( (nTinyBits < long5pow.length) && ((nFractBits + n5bits[nTinyBits]) < 64 ) ){
                /*
                 * We can do this:
                 * take the fraction bits, which are normalized.
                 * (a) nTinyBits == 0: Shift left or right appropriately
                 *     to align the binary point at the extreme right, i.e.
                 *     where a long int point is expected to be. The integer
                 *     result is easily converted to a string.
                 * (b) nTinyBits > 0: Shift right by expShift-nFractBits,
                 *     which effectively converts to long and scales by
                 *     2^nTinyBits. Then multiply by 5^nTinyBits to
                 *     complete the scaling. We know this won't overflow
                 *     because we just counted the number of bits necessary
                 *     in the result. The integer you get from this can
                 *     then be converted to a string pretty easily.
                 */
                long halfULP;
                if ( nTinyBits == 0 ) {
                    if ( binExp > nSignificantBits ){
                        halfULP = 1L << ( binExp-nSignificantBits-1);
                    } else {
                        halfULP = 0L;
                    }
                    if ( binExp >= expShift ){
                        fractBits <<= (binExp-expShift);
                    } else {
                        fractBits >>>= (expShift-binExp) ;
                    }
                    developLongDigits( 0, fractBits, halfULP );
                    return;
                }
                /*
                 * The following causes excess digits to be printed
                 * out in the single-float case. Our manipulation of
                 * halfULP here is apparently not correct. If we
                 * better understand how this works, perhaps we can
                 * use this special case again. But for the time being,
                 * we do not.
                 * else {
                 *     fractBits >>>= expShift+1-nFractBits;
                 *     fractBits *= long5pow[ nTinyBits ];
                 *     halfULP = long5pow[ nTinyBits ] >> (1+nSignificantBits-nFractBits);
                 *     developLongDigits( -nTinyBits, fractBits, halfULP );
                 *     return;
                 * }
                 */
            }
        }
        /*
         * This is the hard case. We are going to compute large positive
         * integers B and S and integer decExp, s.t.
         *      d = ( B / S ) * 10^decExp
         *      1 <= B / S < 10
         * Obvious choices are:
         *      decExp = floor( log10(d) )
         *      B      = d * 2^nTinyBits * 10^max( 0, -decExp )
         *      S      = 10^max( 0, decExp) * 2^nTinyBits
         * (noting that nTinyBits has already been forced to non-negative)
         * I am also going to compute a large positive integer
         *      M      = (1/2^nSignificantBits) * 2^nTinyBits * 10^max( 0, -decExp )
         * i.e. M is (1/2) of the ULP of d, scaled like B.
         * When we iterate through dividing B/S and picking off the
         * quotient bits, we will know when to stop when the remainder
         * is <= M.
         *
         * We keep track of powers of 2 and powers of 5.
         */

        /*
         * Estimate decimal exponent. (If it is small-ish,
         * we could double-check.)
         *
         * First, scale the mantissa bits such that 1 <= d2 < 2.
         * We are then going to estimate
         *          log10(d2) ~=~  (d2-1.5)/1.5 + log(1.5)
         * and so we can estimate
         *      log10(d) ~=~ log10(d2) + binExp * log10(2)
         * take the floor and call it decExp.
         * FIXME -- use more precise constants here. It costs no more.
         */
        double d2 = Double.longBitsToDouble(
            expOne | ( fractBits &~ fractHOB ) );
        decExp = (int)Math.floor(
            (d2-1.5D)*0.289529654D + 0.176091259 + (double)binExp * 0.301029995663981 );
        int B2, B5; // powers of 2 and powers of 5, respectively, in B
        int S2, S5; // powers of 2 and powers of 5, respectively, in S
        int M2, M5; // powers of 2 and powers of 5, respectively, in M
        int Bbits; // binary digits needed to represent B, approx.
        int tenSbits; // binary digits needed to represent 10*S, approx.
        FDBigInt Sval, Bval, Mval;

        B5 = Math.max( 0, -decExp );
        B2 = B5 + nTinyBits + binExp;

        S5 = Math.max( 0, decExp );
        S2 = S5 + nTinyBits;

        M5 = B5;
        M2 = B2 - nSignificantBits;

        /*
         * the long integer fractBits contains the (nFractBits) interesting
         * bits from the mantissa of d ( hidden 1 added if necessary) followed
         * by (expShift+1-nFractBits) zeros. In the interest of compactness,
         * I will shift out those zeros before turning fractBits into a
         * FDBigInt. The resulting whole number will be
         *      d * 2^(nFractBits-1-binExp).
         */
        fractBits >>>= (expShift+1-nFractBits);
        B2 -= nFractBits-1;
        int common2factor = Math.min( B2, S2 );
        B2 -= common2factor;
        S2 -= common2factor;
        M2 -= common2factor;

        /*
         * HACK!! For exact powers of two, the next smallest number
         * is only half as far away as we think (because the meaning of
         * ULP changes at power-of-two bounds) for this reason, we
         * hack M2. Hope this works.
         */
        if ( nFractBits == 1 )
            M2 -= 1;

        if ( M2 < 0 ){
            // oops.
            // since we cannot scale M down far enough,
            // we must scale the other values up.
            B2 -= M2;
            S2 -= M2;
            M2 =  0;
        }
        /*
         * Construct, Scale, iterate.
         * Some day, we'll write a stopping test that takes
         * account of the asymmetry of the spacing of floating-point
         * numbers below perfect powers of 2
         * 26 Sept 96 is not that day.
         * So we use a symmetric test.
         */
        char digits[] = this.digits = new char[18];
        int  ndigit = 0;
        boolean low, high;
        long lowDigitDifference;
        int  q;

        /*
         * Detect the special cases where all the numbers we are about
         * to compute will fit in int or long integers.
         * In these cases, we will avoid doing FDBigInt arithmetic.
         * We use the same algorithms, except that we "normalize"
         * our FDBigInts before iterating. This is to make division easier,
         * as it makes our fist guess (quotient of high-order words)
         * more accurate!
         *
         * Some day, we'll write a stopping test that takes
         * account of the asymmetry of the spacing of floating-point
         * numbers below perfect powers of 2
         * 26 Sept 96 is not that day.
         * So we use a symmetric test.
         */
        Bbits = nFractBits + B2 + (( B5 < n5bits.length )? n5bits[B5] : ( B5*3 ));
        tenSbits = S2+1 + (( (S5+1) < n5bits.length )? n5bits[(S5+1)] : ( (S5+1)*3 ));
        if ( Bbits < 64 && tenSbits < 64){
            if ( Bbits < 32 && tenSbits < 32){
                // wa-hoo! They're all ints!
                int b = ((int)fractBits * small5pow[B5] ) << B2;
                int s = small5pow[S5] << S2;
                int m = small5pow[M5] << M2;
                int tens = s * 10;
                /*
                 * Unroll the first iteration. If our decExp estimate
                 * was too high, our first quotient will be zero. In this
                 * case, we discard it and decrement decExp.
                 */
                ndigit = 0;
                q = b / s;
                b = 10 * ( b % s );
                m *= 10;
                low  = (b <  m );
                high = (b+m > tens );
                assert q < 10 : q; // excessively large digit
                if ( (q == 0) && ! high ){
                    // oops. Usually ignore leading zero.
                    decExp--;
                } else {
                    digits[ndigit++] = (char)('0' + q);
                }
                /*
                 * HACK! Java spec sez that we always have at least
                 * one digit after the . in either F- or E-form output.
                 * Thus we will need more than one digit if we're using
                 * E-form
                 */
                if ( decExp < -3 || decExp >= 8 ){
                    high = low = false;
                }
                while( ! low && ! high ){
                    q = b / s;
                    b = 10 * ( b % s );
                    m *= 10;
                    assert q < 10 : q; // excessively large digit
                    if ( m > 0L ){
                        low  = (b <  m );
                        high = (b+m > tens );
                    } else {
                        // hack -- m might overflow!
                        // in this case, it is certainly > b,
                        // which won't
                        // and b+m > tens, too, since that has overflowed
                        // either!
                        low = true;
                        high = true;
                    }
                    digits[ndigit++] = (char)('0' + q);
                }
                lowDigitDifference = (b<<1) - tens;
            } else {
                // still good! they're all longs!
                long b = (fractBits * long5pow[B5] ) << B2;
                long s = long5pow[S5] << S2;
                long m = long5pow[M5] << M2;
                long tens = s * 10L;
                /*
                 * Unroll the first iteration. If our decExp estimate
                 * was too high, our first quotient will be zero. In this
                 * case, we discard it and decrement decExp.
                 */
                ndigit = 0;
                q = (int) ( b / s );
                b = 10L * ( b % s );
                m *= 10L;
                low  = (b <  m );
                high = (b+m > tens );
                assert q < 10 : q; // excessively large digit
                if ( (q == 0) && ! high ){
                    // oops. Usually ignore leading zero.
                    decExp--;
                } else {
                    digits[ndigit++] = (char)('0' + q);
                }
                /*
                 * HACK! Java spec sez that we always have at least
                 * one digit after the . in either F- or E-form output.
                 * Thus we will need more than one digit if we're using
                 * E-form
                 */
                if ( decExp < -3 || decExp >= 8 ){
                    high = low = false;
                }
                while( ! low && ! high ){
                    q = (int) ( b / s );
                    b = 10 * ( b % s );
                    m *= 10;
                    assert q < 10 : q;  // excessively large digit
                    if ( m > 0L ){
                        low  = (b <  m );
                        high = (b+m > tens );
                    } else {
                        // hack -- m might overflow!
                        // in this case, it is certainly > b,
                        // which won't
                        // and b+m > tens, too, since that has overflowed
                        // either!
                        low = true;
                        high = true;
                    }
                    digits[ndigit++] = (char)('0' + q);
                }
                lowDigitDifference = (b<<1) - tens;
            }
        } else {
            FDBigInt tenSval;
            int  shiftBias;

            /*
             * We really must do FDBigInt arithmetic.
             * Fist, construct our FDBigInt initial values.
             */
            Bval = multPow52( new FDBigInt( fractBits  ), B5, B2 );
            Sval = constructPow52( S5, S2 );
            Mval = constructPow52( M5, M2 );


            // normalize so that division works better
            Bval.lshiftMe( shiftBias = Sval.normalizeMe() );
            Mval.lshiftMe( shiftBias );
            tenSval = Sval.mult( 10 );
            /*
             * Unroll the first iteration. If our decExp estimate
             * was too high, our first quotient will be zero. In this
             * case, we discard it and decrement decExp.
             */
            ndigit = 0;
            q = Bval.quoRemIteration( Sval );
            Mval = Mval.mult( 10 );
            low  = (Bval.cmp( Mval ) < 0);
            high = (Bval.add( Mval ).cmp( tenSval ) > 0 );
            assert q < 10 : q; // excessively large digit
            if ( (q == 0) && ! high ){
                // oops. Usually ignore leading zero.
                decExp--;
            } else {
                digits[ndigit++] = (char)('0' + q);
            }
            /*
             * HACK! Java spec sez that we always have at least
             * one digit after the . in either F- or E-form output.
             * Thus we will need more than one digit if we're using
             * E-form
             */
            if ( decExp < -3 || decExp >= 8 ){
                high = low = false;
            }
            while( ! low && ! high ){
                q = Bval.quoRemIteration( Sval );
                Mval = Mval.mult( 10 );
                assert q < 10 : q;  // excessively large digit
                low  = (Bval.cmp( Mval ) < 0);
                high = (Bval.add( Mval ).cmp( tenSval ) > 0 );
                digits[ndigit++] = (char)('0' + q);
            }
            if ( high && low ){
                Bval.lshiftMe(1);
                lowDigitDifference = Bval.cmp(tenSval);
            } else
                lowDigitDifference = 0L; // this here only for flow analysis!
        }
        this.decExponent = decExp+1;
        this.digits = digits;
        this.nDigits = ndigit;
        /*
         * Last digit gets rounded based on stopping condition.
         */
        if ( high ){
            if ( low ){
                if ( lowDigitDifference == 0L ){
                    // it's a tie!
                    // choose based on which digits we like.
                    if ( (digits[nDigits-1]&1) != 0 ) roundup();
                } else if ( lowDigitDifference > 0 ){
                    roundup();
                }
            } else {
                roundup();
            }
        }
    }

    public String
    toString(){
        // most brain-dead version
        StringBuffer result = new StringBuffer( nDigits+8 );
        if ( isNegative ){ result.append( '-' ); }
        if ( isExceptional ){
            result.append( digits, 0, nDigits );
        } else {
            result.append( "0.");
            result.append( digits, 0, nDigits );
            result.append('e');
            result.append( decExponent );
        }
        return new String(result);
    }

    public String toJavaFormatString() {
        char result[] = (char[])(perThreadBuffer.get());
        int i = getChars(result);
        return new String(result, 0, i);
    }

    public int getChars(char[] result) {
        assert nDigits <= 19 : nDigits; // generous bound on size of nDigits
        int i = 0;
        if (isNegative) { result[0] = '-'; i = 1; }
        if (isExceptional) {
            System.arraycopy(digits, 0, result, i, nDigits);
            i += nDigits;
        } else {
            if (decExponent > 0 && decExponent < 8) {
                // print digits.digits.
                int charLength = Math.min(nDigits, decExponent);
                System.arraycopy(digits, 0, result, i, charLength);
                i += charLength;
                if (charLength < decExponent) {
                    charLength = decExponent-charLength;
                    System.arraycopy(zero, 0, result, i, charLength);
                    i += charLength;
                    result[i++] = '.';
                    result[i++] = '0';
                } else {
                    result[i++] = '.';
                    if (charLength < nDigits) {
                        int t = nDigits - charLength;
                        System.arraycopy(digits, charLength, result, i, t);
                        i += t;
                    } else {
                        result[i++] = '0';
                    }
                }
            } else if (decExponent <=0 && decExponent > -3) {
                result[i++] = '0';
                result[i++] = '.';
                if (decExponent != 0) {
                    System.arraycopy(zero, 0, result, i, -decExponent);
                    i -= decExponent;
                }
                System.arraycopy(digits, 0, result, i, nDigits);
                i += nDigits;
            } else {
                result[i++] = digits[0];
                result[i++] = '.';
                if (nDigits > 1) {
                    System.arraycopy(digits, 1, result, i, nDigits-1);
                    i += nDigits-1;
                } else {
                    result[i++] = '0';
                }
                result[i++] = 'E';
                int e;
                if (decExponent <= 0) {
                    result[i++] = '-';
                    e = -decExponent+1;
                } else {
                    e = decExponent-1;
                }
                // decExponent has 1, 2, or 3, digits
                if (e <= 9) {
                    result[i++] = (char)(e+'0');
                } else if (e <= 99) {
                    result[i++] = (char)(e/10 +'0');
                    result[i++] = (char)(e%10 + '0');
                } else {
                    result[i++] = (char)(e/100+'0');
                    e %= 100;
                    result[i++] = (char)(e/10+'0');
                    result[i++] = (char)(e%10 + '0');
                }
            }
        }
        return i;
    }

    // Per-thread buffer for string/stringbuffer conversion
    @SuppressWarnings("rawtypes")
	private static ThreadLocal perThreadBuffer = new ThreadLocal() {
            protected synchronized Object initialValue() {
                return new char[26];
            }
        };

    private static final int small5pow[] = {
        1,
        5,
        5*5,
        5*5*5,
        5*5*5*5,
        5*5*5*5*5,
        5*5*5*5*5*5,
        5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5*5*5*5*5
    };


    private static final long long5pow[] = {
        1L,
        5L,
        5L*5,
        5L*5*5,
        5L*5*5*5,
        5L*5*5*5*5,
        5L*5*5*5*5*5,
        5L*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
    };

    // approximately ceil( log2( long5pow[i] ) )
    private static final int n5bits[] = {
        0,
        3,
        5,
        7,
        10,
        12,
        14,
        17,
        19,
        21,
        24,
        26,
        28,
        31,
        33,
        35,
        38,
        40,
        42,
        45,
        47,
        49,
        52,
        54,
        56,
        59,
        61,
    };

    private static final char infinity[] = { 'I', 'n', 'f', 'i', 'n', 'i', 't', 'y' };
    private static final char notANumber[] = { 'N', 'a', 'N' };
    private static final char zero[] = { '0', '0', '0', '0', '0', '0', '0', '0' };
}

/*
 * A really, really simple bigint package
 * tailored to the needs of floating base conversion.
 */
class FDBigInt {
    int nWords; // number of words used
    int data[]; // value: data[0] is least significant

    public FDBigInt( long v ){
        data = new int[2];
        data[0] = (int)v;
        data[1] = (int)(v>>>32);
        nWords = (data[1]==0) ? 1 : 2;
    }

    public FDBigInt( FDBigInt other ){
        data = new int[nWords = other.nWords];
        System.arraycopy( other.data, 0, data, 0, nWords );
    }

    private FDBigInt( int [] d, int n ){
        data = d;
        nWords = n;
    }

    /*
     * Left shift by c bits.
     * Shifts this in place.
     */
    public void
    lshiftMe( int c )throws IllegalArgumentException {
        if ( c <= 0 ){
            if ( c == 0 )
                return; // silly.
            else
                throw new IllegalArgumentException("negative shift count");
        }
        int wordcount = c>>5;
        int bitcount  = c & 0x1f;
        int anticount = 32-bitcount;
        int t[] = data;
        int s[] = data;
        if ( nWords+wordcount+1 > t.length ){
            // reallocate.
            t = new int[ nWords+wordcount+1 ];
        }
        int target = nWords+wordcount;
        int src    = nWords-1;
        if ( bitcount == 0 ){
            // special hack, since an anticount of 32 won't go!
            System.arraycopy( s, 0, t, wordcount, nWords );
            target = wordcount-1;
        } else {
            t[target--] = s[src]>>>anticount;
            while ( src >= 1 ){
                t[target--] = (s[src]<<bitcount) | (s[--src]>>>anticount);
            }
            t[target--] = s[src]<<bitcount;
        }
        while( target >= 0 ){
            t[target--] = 0;
        }
        data = t;
        nWords += wordcount + 1;
        // may have constructed high-order word of 0.
        // if so, trim it
        while ( nWords > 1 && data[nWords-1] == 0 )
            nWords--;
    }

    /*
     * normalize this number by shifting until
     * the MSB of the number is at 0x08000000.
     * This is in preparation for quoRemIteration, below.
     * The idea is that, to make division easier, we want the
     * divisor to be "normalized" -- usually this means shifting
     * the MSB into the high words sign bit. But because we know that
     * the quotient will be 0 < q < 10, we would like to arrange that
     * the dividend not span up into another word of precision.
     * (This needs to be explained more clearly!)
     */
    public int
    normalizeMe() throws IllegalArgumentException {
        int src;
        int wordcount = 0;
        int bitcount  = 0;
        int v = 0;
        for ( src= nWords-1 ; src >= 0 && (v=data[src]) == 0 ; src--){
            wordcount += 1;
        }
        if ( src < 0 ){
            // oops. Value is zero. Cannot normalize it!
            throw new IllegalArgumentException("zero value");
        }
        /*
         * In most cases, we assume that wordcount is zero. This only
         * makes sense, as we try not to maintain any high-order
         * words full of zeros. In fact, if there are zeros, we will
         * simply SHORTEN our number at this point. Watch closely...
         */
        nWords -= wordcount;
        /*
         * Compute how far left we have to shift v s.t. its highest-
         * order bit is in the right place. Then call lshiftMe to
         * do the work.
         */
        if ( (v & 0xf0000000) != 0 ){
            // will have to shift up into the next word.
            // too bad.
            for( bitcount = 32 ; (v & 0xf0000000) != 0 ; bitcount-- )
                v >>>= 1;
        } else {
            while ( v <= 0x000fffff ){
                // hack: byte-at-a-time shifting
                v <<= 8;
                bitcount += 8;
            }
            while ( v <= 0x07ffffff ){
                v <<= 1;
                bitcount += 1;
            }
        }
        if ( bitcount != 0 )
            lshiftMe( bitcount );
        return bitcount;
    }

    /*
     * Multiply a FDBigInt by an int.
     * Result is a new FDBigInt.
     */
    public FDBigInt
    mult( int iv ) {
        long v = iv;
        int r[];
        long p;

        // guess adequate size of r.
        r = new int[ ( v * ((long)data[nWords-1]&0xffffffffL) > 0xfffffffL ) ? nWords+1 : nWords ];
        p = 0L;
        for( int i=0; i < nWords; i++ ) {
            p += v * ((long)data[i]&0xffffffffL);
            r[i] = (int)p;
            p >>>= 32;
        }
        if ( p == 0L){
            return new FDBigInt( r, nWords );
        } else {
            r[nWords] = (int)p;
            return new FDBigInt( r, nWords+1 );
        }
    }

    /*
     * Multiply a FDBigInt by another FDBigInt.
     * Result is a new FDBigInt.
     */
    public FDBigInt
    mult( FDBigInt other ){
        // crudely guess adequate size for r
        int r[] = new int[ nWords + other.nWords ];
        int i;
        // I think I am promised zeros...

        for( i = 0; i < this.nWords; i++ ){
            long v = (long)this.data[i] & 0xffffffffL; // UNSIGNED CONVERSION
            long p = 0L;
            int j;
            for( j = 0; j < other.nWords; j++ ){
                p += ((long)r[i+j]&0xffffffffL) + v*((long)other.data[j]&0xffffffffL); // UNSIGNED CONVERSIONS ALL 'ROUND.
                r[i+j] = (int)p;
                p >>>= 32;
            }
            r[i+j] = (int)p;
        }
        // compute how much of r we actually needed for all that.
        for ( i = r.length-1; i> 0; i--)
            if ( r[i] != 0 )
                break;
        return new FDBigInt( r, i+1 );
    }

    /*
     * Add one FDBigInt to another. Return a FDBigInt
     */
    public FDBigInt
    add( FDBigInt other ){
        int i;
        int a[], b[];
        int n, m;
        long c = 0L;
        // arrange such that a.nWords >= b.nWords;
        // n = a.nWords, m = b.nWords
        if ( this.nWords >= other.nWords ){
            a = this.data;
            n = this.nWords;
            b = other.data;
            m = other.nWords;
        } else {
            a = other.data;
            n = other.nWords;
            b = this.data;
            m = this.nWords;
        }
        int r[] = new int[ n ];
        for ( i = 0; i < n; i++ ){
            c += (long)a[i] & 0xffffffffL;
            if ( i < m ){
                c += (long)b[i] & 0xffffffffL;
            }
            r[i] = (int) c;
            c >>= 32; // signed shift.
        }
        if ( c != 0L ){
            // oops -- carry out -- need longer result.
            int s[] = new int[ r.length+1 ];
            System.arraycopy( r, 0, s, 0, r.length );
            s[i++] = (int)c;
            return new FDBigInt( s, i );
        }
        return new FDBigInt( r, i );
    }

    /*
     * Compare FDBigInt with another FDBigInt. Return an integer
     * >0: this > other
     *  0: this == other
     * <0: this < other
     */
    public int
    cmp( FDBigInt other ){
        int i;
        if ( this.nWords > other.nWords ){
            // if any of my high-order words is non-zero,
            // then the answer is evident
            int j = other.nWords-1;
            for ( i = this.nWords-1; i > j ; i-- )
                if ( this.data[i] != 0 ) return 1;
        }else if ( this.nWords < other.nWords ){
            // if any of other's high-order words is non-zero,
            // then the answer is evident
            int j = this.nWords-1;
            for ( i = other.nWords-1; i > j ; i-- )
                if ( other.data[i] != 0 ) return -1;
        } else{
            i = this.nWords-1;
        }
        for ( ; i > 0 ; i-- )
            if ( this.data[i] != other.data[i] )
                break;
        // careful! want unsigned compare!
        // use brute force here.
        int a = this.data[i];
        int b = other.data[i];
        if ( a < 0 ){
            // a is really big, unsigned
            if ( b < 0 ){
                return a-b; // both big, negative
            } else {
                return 1; // b not big, answer is obvious;
            }
        } else {
            // a is not really big
            if ( b < 0 ) {
                // but b is really big
                return -1;
            } else {
                return a - b;
            }
        }
    }

    /*
     * Compute
     * q = (int)( this / S )
     * this = 10 * ( this mod S )
     * Return q.
     * This is the iteration step of digit development for output.
     * We assume that S has been normalized, as above, and that
     * "this" has been lshift'ed accordingly.
     * Also assume, of course, that the result, q, can be expressed
     * as an integer, 0 <= q < 10.
     */
    public int
    quoRemIteration( FDBigInt S )throws IllegalArgumentException {
        // ensure that this and S have the same number of
        // digits. If S is properly normalized and q < 10 then
        // this must be so.
        if ( nWords != S.nWords ){
            throw new IllegalArgumentException("disparate values");
        }
        // estimate q the obvious way. We will usually be
        // right. If not, then we're only off by a little and
        // will re-add.
        int n = nWords-1;
        long q = ((long)data[n]&0xffffffffL) / (long)S.data[n];
        long diff = 0L;
        for ( int i = 0; i <= n ; i++ ){
            diff += ((long)data[i]&0xffffffffL) -  q*((long)S.data[i]&0xffffffffL);
            data[i] = (int)diff;
            diff >>= 32; // N.B. SIGNED shift.
        }
        if ( diff != 0L ) {
            // damn, damn, damn. q is too big.
            // add S back in until this turns +. This should
            // not be very many times!
            long sum = 0L;
            while ( sum ==  0L ){
                sum = 0L;
                for ( int i = 0; i <= n; i++ ){
                    sum += ((long)data[i]&0xffffffffL) +  ((long)S.data[i]&0xffffffffL);
                    data[i] = (int) sum;
                    sum >>= 32; // Signed or unsigned, answer is 0 or 1
                }
                /*
                 * Originally the following line read
                 * "if ( sum !=0 && sum != -1 )"
                 * but that would be wrong, because of the
                 * treatment of the two values as entirely unsigned,
                 * it would be impossible for a carry-out to be interpreted
                 * as -1 -- it would have to be a single-bit carry-out, or
                 * +1.
                 */
                assert sum == 0 || sum == 1 : sum; // carry out of division correction
                q -= 1;
            }
        }
        // finally, we can multiply this by 10.
        // it cannot overflow, right, as the high-order word has
        // at least 4 high-order zeros!
        long p = 0L;
        for ( int i = 0; i <= n; i++ ){
            p += 10*((long)data[i]&0xffffffffL);
            data[i] = (int)p;
            p >>= 32; // SIGNED shift.
        }
        assert p == 0L : p; // Carry out of *10
        return (int)q;
    }

    public String
    toString() {
        StringBuffer r = new StringBuffer(30);
        r.append('[');
        int i = Math.min( nWords-1, data.length-1) ;
        if ( nWords > data.length ){
            r.append( "("+data.length+"<"+nWords+"!)" );
        }
        for( ; i> 0 ; i-- ){
            r.append( Integer.toHexString( data[i] ) );
            r.append(' ');
        }
        r.append( Integer.toHexString( data[0] ) );
        r.append(']');
        return new String( r );
    }
}
