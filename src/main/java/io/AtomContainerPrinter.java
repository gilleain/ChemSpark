package io;

import group.*;
import org.openscience.cdk.interfaces.*;

import java.util.*;

public class AtomContainerPrinter {

    public static void print(IAtomContainer atomContainer) {
        System.out.println(AtomContainerPrinter.toString(atomContainer));
    }

    public static String toString(IAtomContainer atomContainer) {
        return AtomContainerPrinter.toString(atomContainer, new Permutation(atomContainer.getAtomCount()));
    }

    public static String toString(IAtomContainer atomContainer, Permutation permutation) {
        return toString(atomContainer, permutation, false); // don't sort by default?
    }

    public static String toString(IAtomContainer atomContainer, Permutation permutation, boolean sortEdges) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < atomContainer.getAtomCount(); i++) {
            IAtom atomI = atomContainer.getAtom(permutation.get(i));
            sb.append(atomI.getSymbol()).append(i);
        }
        sb.append(" ");

        int i = 0;
        List<String> edgeStrings = null;
        if (sortEdges) {
            edgeStrings = new ArrayList<String>();
        }
        for (IBond bond : atomContainer.bonds()) {
            int a0 = atomContainer.getAtomNumber(bond.getAtom(0));
            int a1 = atomContainer.getAtomNumber(bond.getAtom(1));
            int pA0 = permutation.get(a0);
            int pA1 = permutation.get(a1);
            char o = bondOrderToChar(bond.getOrder());
            if (sortEdges) {
                String edgeString;
                if (pA0 < pA1) {
                    edgeString = pA0 + ":" + pA1 + "(" + o + ")";
                }
                else {
                    edgeString = pA1 + ":" + pA0 + "(" + o + ")";
                }
                edgeStrings.add(edgeString);
            }
            else {
                if (pA0 < pA1) {
                    sb.append(pA0 + ":" + pA1 + "(" + o + ")");
                }
                else {
                    sb.append(pA1 + ":" + pA0 + "(" + o + ")");
                }
            }
            if (!sortEdges && i < atomContainer.getBondCount() - 1) {
                sb.append(",");
            }
            i++;
        }
        if (sortEdges) {
            Collections.sort(edgeStrings);
            i = 0;
            for (String edgeString : edgeStrings) {
                sb.append(edgeString);
                if (i < atomContainer.getBondCount() - 1) {
                    sb.append(",");
                }
            }
        }
        return sb.toString();
    }

    private static char bondOrderToChar(IBond.Order order) {
        switch (order) {
            case SINGLE:
                return '1';
            case DOUBLE:
                return '2';
            case TRIPLE:
                return '3';
            case QUADRUPLE:
                return '4';
        //    case UNSET:
         //       return '?';
            default:
                return '?';
        }
    }

    private static IBond.Order charToBondOrder(char orderChar) {
        switch (orderChar) {
            case '1':
                return IBond.Order.SINGLE;
            case '2':
                return IBond.Order.DOUBLE;
            case '3':
                return IBond.Order.TRIPLE;
            case '4':
                return IBond.Order.QUADRUPLE;
     //       case '?':                          // out for 1.4.9 slewis
     //           return IBond.Order.UNSET;
            default:
                throw new UnsupportedOperationException("Fix This"); // ToDo
              //  return IBond.Order.UNSET;    // out for 1.4.9 slewis
        }
    }

    public static IAtomContainer fromString(String acpString, IChemObjectBuilder builder) {
        int gapIndex = acpString.indexOf(' ');
        if (gapIndex == -1) return null;    // TODO : raise error

        IAtomContainer atomContainer = builder.newInstance(IAtomContainer.class);
        String elementString = acpString.substring(0, gapIndex);
        // skip the atom number, as this is just a visual convenience
        for (int index = 0; index < elementString.length(); index += 2) {
            String elementSymbol = String.valueOf(elementString.charAt(index));
            atomContainer.addAtom(builder.newInstance(IAtom.class, elementSymbol));
        }

        String bondString = acpString.substring(gapIndex + 1);
        for (String bondPart : bondString.split(",")) {
            int colonIndex = bondPart.indexOf(':');
            int openBracketIndex = bondPart.indexOf('(');
            int closeBracketIndex = bondPart.indexOf(')');
            int a0 = Integer.parseInt(bondPart.substring(0, colonIndex));
            int a1 = Integer.parseInt(bondPart.substring(colonIndex + 1, openBracketIndex));
            char o = bondPart.substring(openBracketIndex + 1, closeBracketIndex).charAt(0);
            atomContainer.addBond(a0, a1, charToBondOrder(o));
        }
        return atomContainer;
    }
}
