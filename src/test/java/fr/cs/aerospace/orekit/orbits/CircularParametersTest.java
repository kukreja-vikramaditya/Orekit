package fr.cs.aerospace.orekit.orbits;

import junit.framework.*;

import org.spaceroots.mantissa.geometry.Vector3D;

import fr.cs.aerospace.orekit.Utils;
import fr.cs.aerospace.orekit.orbits.CircularParameters;
import fr.cs.aerospace.orekit.orbits.EquinoctialParameters;
import fr.cs.aerospace.orekit.orbits.KeplerianParameters;

public class CircularParametersTest extends TestCase {

  public CircularParametersTest(String name) {
    super(name);
  }

  public void testCircularToEquinoctialEll() {
  
    double ix = 1.200e-04;
    double iy = -1.16e-04;
    double i  = 2 * Math.asin(Math.sqrt((ix * ix + iy * iy) / 4));
    double raan = Math.atan2(iy, ix);
 
    // elliptic orbit
    CircularParameters circ =
      new CircularParameters(42166.712, 0.5, -0.5, i, raan,
                             5.300 - raan, CircularParameters.MEAN_LONGITUDE_ARGUMENT);
    double mu = 3.9860047e14;
    Vector3D pos = circ.getPosition(mu);
    Vector3D vit = circ.getVelocity(mu);
  
    EquinoctialParameters param = new EquinoctialParameters(pos,vit,mu);
    assertEquals(param.getA(),  circ.getA(), Utils.epsilonTest * circ.getA());
    assertEquals(param.getEquinoctialEx(), circ.getEquinoctialEx(), Utils.epsilonE * Math.abs(circ.getE()));
    assertEquals(param.getEquinoctialEy(), circ.getEquinoctialEy(), Utils.epsilonE * Math.abs(circ.getE()));
    assertEquals(param.getHx(), circ.getHx(), Utils.epsilonAngle * Math.abs(circ.getI()));
    assertEquals(param.getHy(), circ.getHy(), Utils.epsilonAngle * Math.abs(circ.getI()));
    assertEquals(Utils.trimAngle(param.getLv(),circ.getLv()), circ.getLv(), Utils.epsilonAngle * Math.abs(circ.getLv()));

  }

  public void testCircularToEquinoctialCirc() {

    double ix = 1.200e-04;
    double iy = -1.16e-04;
    double i  = 2 * Math.asin(Math.sqrt((ix * ix + iy * iy) / 4));
    double raan = Math.atan2(iy, ix);

    // circular orbit
    EquinoctialParameters circCir =
      new EquinoctialParameters(42166.712, 0.1e-10, -0.1e-10, i, raan,
                                5.300 - raan, CircularParameters.MEAN_LONGITUDE_ARGUMENT);
    double mu = 3.9860047e14;
    Vector3D posCir = circCir.getPosition(mu);
    Vector3D vitCir = circCir.getVelocity(mu);
  
    EquinoctialParameters paramCir = new EquinoctialParameters(posCir,vitCir,mu);
    assertEquals(paramCir.getA(), circCir.getA(), Utils.epsilonTest * circCir.getA());
    assertEquals(paramCir.getEquinoctialEx(), circCir.getEquinoctialEx(), Utils.epsilonEcir * Math.abs(circCir.getE()));
    assertEquals(paramCir.getEquinoctialEy(), circCir.getEquinoctialEy(), Utils.epsilonEcir * Math.abs(circCir.getE()));
    assertEquals(paramCir.getHx(), circCir.getHx(), Utils.epsilonAngle * Math.abs(circCir.getI()));
    assertEquals(paramCir.getHy(), circCir.getHy(), Utils.epsilonAngle * Math.abs(circCir.getI()));
    assertEquals(Utils.trimAngle(paramCir.getLv(),circCir.getLv()), circCir.getLv(), Utils.epsilonAngle * Math.abs(circCir.getLv()));
 
  }

  public void testCircularToCartesian() {
    
    double ix = 1.200e-04;
    double iy = -1.16e-04;
    double i  = 2 * Math.asin(Math.sqrt((ix * ix + iy * iy) / 4));
    double raan = Math.atan2(iy, ix);
    double cosRaan = Math.cos(raan);
    double sinRaan = Math.sin(raan);
    double exTilde = -7.900e-6;
    double eyTilde = 1.100e-4;
    double ex = exTilde * cosRaan + eyTilde * sinRaan;
    double ey = eyTilde * cosRaan - exTilde * sinRaan;

    CircularParameters circ=
      new CircularParameters(42166.712, ex, ey, i, raan,
                             5.300 - raan, CircularParameters.MEAN_LONGITUDE_ARGUMENT); 
    double mu = 3.9860047e14;
    Vector3D pos = circ.getPosition(mu);
    Vector3D vel = circ.getVelocity(mu);

    // check 1/a = 2/r  - V2/mu
    double r = pos.getNorm();
    double v = vel.getNorm();
    assertEquals(2 / r - v * v / mu, 1 / circ.getA(), 1.0e-7);

    assertEquals( 0.233745668678733e+05, pos.getX(), Utils.epsilonTest * r);
    assertEquals(-0.350998914352669e+05, pos.getY(), Utils.epsilonTest * r);
    assertEquals(-0.150053723123334e+01, pos.getZ(), Utils.epsilonTest * r);
    
    assertEquals(0.809135038364960e+05, vel.getX(), Utils.epsilonTest * v);
    assertEquals(0.538902268252598e+05, vel.getY(), Utils.epsilonTest * v);
    assertEquals(0.158527938296630e+02, vel.getZ(), Utils.epsilonTest * v);
    
  }

  public void testCircularToKeplerian() {
  
    double ix   =  1.20e-4;
    double iy   = -1.16e-4;
    double i    = 2 * Math.asin(Math.sqrt((ix * ix + iy * iy) / 4));
    double raan = Math.atan2(iy, ix);
    double cosRaan = Math.cos(raan);
    double sinRaan = Math.sin(raan);
    double exTilde = -7.900e-6;
    double eyTilde = 1.100e-4;
    double ex = exTilde * cosRaan + eyTilde * sinRaan;
    double ey = eyTilde * cosRaan - exTilde * sinRaan;
  
    CircularParameters circ=
      new CircularParameters(42166.712, ex, ey, i, raan,
                             5.300 - raan, CircularParameters.MEAN_LONGITUDE_ARGUMENT); 
    KeplerianParameters kep = new KeplerianParameters(circ, 3.9860047e14);
  
    assertEquals(42166.71200, circ.getA(), Utils.epsilonTest * kep.getA());
    assertEquals(0.110283316961361e-03, kep.getE(), Utils.epsilonE * Math.abs(kep.getE()));
    assertEquals(0.166901168553917e-03, kep.getI(),
                 Utils.epsilonAngle * Math.abs(kep.getI()));
    assertEquals(Utils.trimAngle(-3.87224326008837, kep.getPerigeeArgument()),
                 kep.getPerigeeArgument(),
                 Utils.epsilonTest * Math.abs(kep.getPerigeeArgument()));
    assertEquals(Utils.trimAngle(5.51473467358854, kep.getRightAscensionOfAscendingNode()),
                 kep.getRightAscensionOfAscendingNode(),
                 Utils.epsilonTest * Math.abs(kep.getRightAscensionOfAscendingNode()));
    assertEquals(Utils.trimAngle(3.65750858649982, kep.getMeanAnomaly()),
                 kep.getMeanAnomaly(),
                 Utils.epsilonTest * Math.abs(kep.getMeanAnomaly()));
  
  }
  
  public void testAnomalyEll() {
    
    // elliptic orbit
    Vector3D position = new Vector3D(7.0e6, 1.0e6, 4.0e6);
    Vector3D velocity = new Vector3D(-500.0, 8000.0, 1000.0);
    double mu = 3.9860047e14;
    
    CircularParameters  p   = new CircularParameters(position, velocity, mu);
    KeplerianParameters kep = new KeplerianParameters(p, 3.9860047e14);

    double e       = p.getE();
    double eRatio  = Math.sqrt((1 - e) / (1 + e));
    double raan    = kep.getRightAscensionOfAscendingNode();
    double paPraan = kep.getPerigeeArgument() + raan;
    
    double lv = 1.1;
    // formulations for elliptic case 
    double lE = 2 * Math.atan(eRatio * Math.tan((lv - paPraan) / 2)) + paPraan;
    double lM = lE - e * Math.sin(lE - paPraan);

    p.setAlphaV(lv - raan);
    assertEquals(p.getAlphaV() + raan, lv, Utils.epsilonAngle * Math.abs(lv));
    assertEquals(p.getAlphaE() + raan, lE, Utils.epsilonAngle * Math.abs(lE));
    assertEquals(p.getAlphaM() + raan, lM, Utils.epsilonAngle * Math.abs(lM));
    p.setAlphaV(0);

    p.setAlphaE(lE - raan);
    assertEquals(p.getAlphaV() + raan, lv, Utils.epsilonAngle * Math.abs(lv));
    assertEquals(p.getAlphaE() + raan, lE, Utils.epsilonAngle * Math.abs(lE));
    assertEquals(p.getAlphaM() + raan, lM, Utils.epsilonAngle * Math.abs(lM));
    p.setAlphaV(0);

    p.setAlphaM(lM - raan);
    assertEquals(p.getAlphaV() + raan, lv, Utils.epsilonAngle * Math.abs(lv));
    assertEquals(p.getAlphaE() + raan, lE, Utils.epsilonAngle * Math.abs(lE));
    assertEquals(p.getAlphaM() + raan, lM, Utils.epsilonAngle * Math.abs(lM));

  }

  public void testAnomalyCirc() {

    Vector3D position = new Vector3D(7.0e6, 1.0e6, 4.0e6);
    Vector3D velocity = new Vector3D(-500.0, 8000.0, 1000.0);
    double mu = 3.9860047e14;
    CircularParameters  p = new CircularParameters(position, velocity, mu);
    double raan = p.getRightAscensionOfAscendingNode();

    // circular orbit
    p.setCircularEx(0);
    p.setCircularEy(0);
    
    double lv = 1.1;
    double lE = lv;
    double lM = lE;

    p.setAlphaV(lv - raan);
    assertEquals(p.getAlphaV() + raan, lv, Utils.epsilonAngle * Math.abs(lv));
    assertEquals(p.getAlphaE() + raan, lE, Utils.epsilonAngle * Math.abs(lE));
    assertEquals(p.getAlphaM() + raan, lM, Utils.epsilonAngle * Math.abs(lM));
    p.setAlphaV(0);

    p.setAlphaE(lE - raan);
    assertEquals(p.getAlphaV() + raan, lv, Utils.epsilonAngle * Math.abs(lv));
    assertEquals(p.getAlphaE() + raan, lE, Utils.epsilonAngle * Math.abs(lE));
    assertEquals(p.getAlphaM() + raan, lM, Utils.epsilonAngle * Math.abs(lM));
    p.setAlphaV(0);

    p.setAlphaM(lM - raan);
    assertEquals(p.getAlphaV() + raan, lv, Utils.epsilonAngle * Math.abs(lv));
    assertEquals(p.getAlphaE() + raan, lE, Utils.epsilonAngle * Math.abs(lE));
    assertEquals(p.getAlphaM() + raan, lM, Utils.epsilonAngle * Math.abs(lM));

  }

  public void testPositionVelocityNormsEll() {
    
    double mu = 3.9860047e14;
    
    // elliptic and non equatorial (i retrograde) orbit
    double hx =  1.2;
    double hy =  2.1;
    double i  = 2 * Math.atan(Math.sqrt(hx * hx + hy * hy));
    double raan = Math.atan2(hy, hx);
    CircularParameters p =
      new CircularParameters(42166.712, 0.5, -0.5, i, raan,
                             0.67 - raan, CircularParameters.TRUE_LONGITUDE_ARGUMENT);
    
    double ex = p.getEquinoctialEx();
    double ey = p.getEquinoctialEy();
    double lv = p.getLv();
    double ksi     = 1 + ex * Math.cos(lv) + ey * Math.sin(lv);
    double nu      = ex * Math.sin(lv) - ey * Math.cos(lv);
    double epsilon = Math.sqrt(1 - ex * ex - ey * ey);

    double a  = p.getA();
    double na = Math.sqrt(mu / a);

    assertEquals(a * epsilon * epsilon / ksi,
                 p.getPosition(mu).getNorm(),
                 Utils.epsilonTest * Math.abs(p.getPosition(mu).getNorm()));
    assertEquals(na * Math.sqrt(ksi * ksi + nu * nu) / epsilon,
                 p.getVelocity(mu).getNorm(),
                 Utils.epsilonTest * Math.abs(p.getVelocity(mu).getNorm()));

  }

  public void testPositionVelocityNormsCirc() {
    
    double mu = 3.9860047e14;
    
    // elliptic and non equatorial (i retrograde) orbit
    double hx =  0.1e-8;
    double hy =  0.1e-8;
    double i  = 2 * Math.atan(Math.sqrt(hx * hx + hy * hy));
    double raan = Math.atan2(hy, hx);
    CircularParameters pCirEqua =
      new CircularParameters(42166.712, 0.1e-8, 0.1e-8, i, raan,
                             0.67 - raan, CircularParameters.TRUE_LONGITUDE_ARGUMENT);
 
    double ex = pCirEqua.getEquinoctialEx();
    double ey = pCirEqua.getEquinoctialEy();
    double lv = pCirEqua.getLv();
    double ksi     = 1 + ex * Math.cos(lv) + ey * Math.sin(lv);
    double nu      = ex * Math.sin(lv) - ey * Math.cos(lv);
    double epsilon = Math.sqrt(1 - ex * ex - ey * ey);
    
    double a  = pCirEqua.getA();
    double na = Math.sqrt(mu / a);

    assertEquals(a * epsilon * epsilon / ksi,
                 pCirEqua.getPosition(mu).getNorm(),
                 Utils.epsilonTest * Math.abs(pCirEqua.getPosition(mu).getNorm()));
    assertEquals(na * Math.sqrt(ksi * ksi + nu * nu) / epsilon,
                 pCirEqua.getVelocity(mu).getNorm(),
                 Utils.epsilonTest * Math.abs(pCirEqua.getVelocity(mu).getNorm()));
  }

  public void testGeometryEll() {
    double mu = 3.9860047e14;
    
    // elliptic and non equatorial (i retrograde) orbit 
    double hx =  1.2;
    double hy =  2.1;
    double i  = 2 * Math.atan(Math.sqrt(hx * hx + hy * hy));
    double raan = Math.atan2(hy, hx);
    CircularParameters p =
      new CircularParameters(42166.712, 0.5, -0.5, i, raan,
                                0.67 - raan, CircularParameters.TRUE_LONGITUDE_ARGUMENT);
    
    Vector3D position = p.getPosition(mu);
    Vector3D velocity = p.getVelocity(mu);
 
    Vector3D momentum = Vector3D.crossProduct(position,velocity);
    momentum.normalizeSelf();
    
    double apogeeRadius  = p.getA() * (1 + p.getE());
    double perigeeRadius = p.getA() * (1 - p.getE());
    
    for (double alphaV = 0; alphaV <= 2 * Math.PI; alphaV += 2 * Math.PI/100.) {
      p.setAlphaV(alphaV);
      position = p.getPosition(mu);
      
      // test if the norm of the position is in the range [perigee radius, apogee radius]
      // Warning: these tests are without absolute value by choice
      assertTrue((position.getNorm() - apogeeRadius)  <= (  apogeeRadius * Utils.epsilonTest));
      assertTrue((position.getNorm() - perigeeRadius) >= (- perigeeRadius * Utils.epsilonTest));
      
      position.normalizeSelf();
      velocity = p.getVelocity(mu);
      velocity.normalizeSelf();
      
      // at this stage of computation, all the vectors (position, velocity and momemtum) are normalized here
      
      // test of orthogonality between position and momentum
      assertTrue(Math.abs(Vector3D.dotProduct(position, momentum)) < Utils.epsilonTest);
      // test of orthogonality between velocity and momentum
      assertTrue(Math.abs(Vector3D.dotProduct(velocity, momentum)) < Utils.epsilonTest);
    }

  }

  public void testGeometryCirc() {
    double mu = 3.9860047e14;
    //  circular and equatorial orbit
    double hx =  0.1e-8;
    double hy =  0.1e-8;
    double i  = 2 * Math.atan(Math.sqrt(hx * hx + hy * hy));
    double raan = Math.atan2(hy, hx);
    CircularParameters pCirEqua =
      new CircularParameters(42166.712, 0.1e-8, 0.1e-8, i, raan,
                                0.67 - raan, CircularParameters.TRUE_LONGITUDE_ARGUMENT);
    
    Vector3D position = pCirEqua.getPosition(mu);
    Vector3D velocity = pCirEqua.getVelocity(mu);
    
    Vector3D momentum = Vector3D.crossProduct(position,velocity);
    momentum.normalizeSelf();
    
    double apogeeRadius  = pCirEqua.getA() * (1 + pCirEqua.getE());
    double perigeeRadius = pCirEqua.getA() * (1 - pCirEqua.getE());
    // test if apogee equals perigee
    assertEquals(perigeeRadius, apogeeRadius, 1.e+4 * Utils.epsilonTest * apogeeRadius);
 
    for (double alphaV = 0; alphaV <= 2 * Math.PI; alphaV += 2 * Math.PI/100.) {
      pCirEqua.setAlphaV(alphaV);
      position = pCirEqua.getPosition(mu);
      
      // test if the norm pf the position is in the range [perigee radius, apogee radius]
      assertTrue((position.getNorm() - apogeeRadius)  <= (  apogeeRadius * Utils.epsilonTest));
      assertTrue((position.getNorm() - perigeeRadius) >= (- perigeeRadius * Utils.epsilonTest));
      
      position.normalizeSelf();
      velocity = pCirEqua.getVelocity(mu);
      velocity.normalizeSelf();
      
      // at this stage of computation, all the vectors (position, velocity and momemtum) are normalized here
      
      // test of orthogonality between position and momentum
      assertTrue(Math.abs(Vector3D.dotProduct(position, momentum)) < Utils.epsilonTest);
      // test of orthogonality between velocity and momentum
      assertTrue(Math.abs(Vector3D.dotProduct(velocity, momentum)) < Utils.epsilonTest);
    }
  }
  

  public void testSymmetryEll() {

    // elliptic and non equatorail orbit
     Vector3D position = new Vector3D(4512.9, 18260., -5127.);
     Vector3D velocity = new Vector3D(134664.6, 90066.8, 72047.6);
     double mu = 3.9860047e14;
     
     CircularParameters p = new CircularParameters(position, velocity, mu);

     Vector3D positionOffset = new Vector3D(p.getPosition(mu));
     Vector3D velocityOffset = new Vector3D(p.getVelocity(mu));
     
     positionOffset.subtractFromSelf(position);
     velocityOffset.subtractFromSelf(velocity);

     assertEquals(0.0, positionOffset.getNorm(), position.getNorm() * Utils.epsilonTest);
     assertEquals(0.0, velocityOffset.getNorm(), velocity.getNorm() * Utils.epsilonTest);

  }

  public void testSymmetryCir() {
     // circular and equatorial orbit
    Vector3D position = new Vector3D(33051.2, 26184.9, -1.3E-5);
    Vector3D velocity = new Vector3D(-60376.2, 76208., 2.7E-4);
    double mu = 3.9860047e14;
    
    CircularParameters p = new CircularParameters(position, velocity, mu);

     Vector3D positionOffset = new Vector3D(p.getPosition(mu));
     Vector3D velocityOffset = new Vector3D(p.getVelocity(mu));
     
     positionOffset.subtractFromSelf(position);
     velocityOffset.subtractFromSelf(velocity);

     assertEquals(0.0, positionOffset.getNorm(), position.getNorm() * Utils.epsilonTest);
     assertEquals(0.0, velocityOffset.getNorm(), velocity.getNorm() * Utils.epsilonTest);

  }
  
  public static Test suite() {
    return new TestSuite(CircularParametersTest.class);
  }
  
}
