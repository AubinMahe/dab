package org.hpms.dab.dsl.scoping

import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.scoping.Scopes
import org.hpms.dab.dsl.dal.DalPackage
import org.hpms.dab.dsl.dal.QName
import org.hpms.dab.dsl.dal.UserType
import org.hpms.dab.dsl.dal.connection
import org.hpms.dab.dsl.dal.deployment
import org.hpms.dab.dsl.dal.instance

/**
 * This class contains custom scoping description.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#scoping
 * on how and when to use it.
 */
class DALScopeProvider extends AbstractDALScopeProvider {

   override getScope(EObject context, EReference reference) {
      if( context instanceof connection && reference == DalPackage.Literals.CONNECTION__INSTANCES ) {
         val deployment = EcoreUtil2.getContainerOfType( context, deployment );
         val candidates = EcoreUtil2.getAllContentsOfType( deployment, instance )
         return Scopes.scopeFor( candidates )
      }
      else if( context instanceof QName && reference == DalPackage.Literals.QNAME__TYPE ) {
         val ctxt = context as QName
         val candidates = EcoreUtil2.getAllContentsOfType( ctxt.lib, UserType )
         return Scopes.scopeFor( candidates )
      }
      return super.getScope(context, reference);
   }
}
