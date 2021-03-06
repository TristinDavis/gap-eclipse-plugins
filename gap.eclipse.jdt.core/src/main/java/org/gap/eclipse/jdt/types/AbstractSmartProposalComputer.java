package org.gap.eclipse.jdt.types;

import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.gap.eclipse.jdt.CorePlugin;
import org.osgi.framework.Version;

import com.google.common.collect.Sets;

public class AbstractSmartProposalComputer {

	protected static final long TIMEOUT = Long.getLong("org.gap.eclipse.jdt.types.smartSearchTimeout", defaultTimeout());
	private Set<String> unsupportedTypes = Sets.newHashSet("java.lang.String", "java.lang.Object",
			"java.lang.Cloneable", "java.lang.Throwable", "java.lang.Exception");

	public AbstractSmartProposalComputer() {
		super();
	}

	private static long defaultTimeout() {
		Version version = Platform.getProduct().getDefiningBundle().getVersion();
		
		if((version.getMajor() == 4) && (version.getMinor() > 16) ||
				version.getMajor() > 4) {
			return 10000;
		}
		return 4000;
	}

	protected final CompletionProposal createImportProposal(JavaContentAssistInvocationContext context, IType type)
			throws JavaModelException {
		CompletionProposal proposal = CompletionProposal.create(CompletionProposal.TYPE_IMPORT,
				context.getInvocationOffset());
		String fullyQualifiedName = type.getFullyQualifiedName();
		proposal.setCompletion(fullyQualifiedName.toCharArray());
		proposal.setDeclarationSignature(type.getPackageFragment().getElementName().toCharArray());
		proposal.setFlags(type.getFlags());
		proposal.setSignature(Signature.createTypeSignature(fullyQualifiedName, true).toCharArray());

		return proposal;
	}
	
	protected final boolean shouldCompute(ContentAssistInvocationContext context) {
		if(context instanceof JavaContentAssistInvocationContext) {
			JavaContentAssistInvocationContext jcontext = (JavaContentAssistInvocationContext) context;
			try {
				return !".".equals(jcontext.getDocument().get(context.getInvocationOffset() - 1, 1)) &&
						jcontext.getCoreContext().getTokenStart() > 0 &&
						!".".equals(jcontext.getDocument().get(jcontext.getCoreContext().getTokenStart() - 1, 1));
			} catch (BadLocationException e) {
				CorePlugin.getDefault().logError(e.getMessage(), e);
			}
		}
		return false;
	}
	
	protected final boolean isUnsupportedType(String fqn) {
		return unsupportedTypes.contains(fqn);
	}
	
	protected final String toParameterizeFQN(char[] signature) {
		final String sig = String.valueOf(signature);
		final String qualifier = Signature.getSignatureQualifier(sig);
		final String name = Signature.getSignatureSimpleName(sig);
		return qualifier.concat(".").concat(name);
	}
}
