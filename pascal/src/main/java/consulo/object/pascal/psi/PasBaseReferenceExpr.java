package consulo.object.pascal.psi;

import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasGenericPostfix;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 28/06/2021
 */
public interface PasBaseReferenceExpr extends PasExpr {
    @Nullable
    PasExpr getExpr();

    @Nonnull
    PasFullyQualifiedIdent getFullyQualifiedIdent();

    @Nullable
    PasGenericPostfix getGenericPostfix();
}