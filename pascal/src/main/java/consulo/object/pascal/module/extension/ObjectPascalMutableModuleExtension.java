package consulo.object.pascal.module.extension;

import consulo.module.extension.MutableModuleExtensionWithSdk;

/**
 * @author VISTALL
 * @since 27/06/2021
 */
public interface ObjectPascalMutableModuleExtension<T extends ObjectPascalModuleExtension<T>> extends ObjectPascalModuleExtension<T>, MutableModuleExtensionWithSdk<T> {
}
