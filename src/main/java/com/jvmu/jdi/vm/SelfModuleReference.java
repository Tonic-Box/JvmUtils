package com.jvmu.jdi.vm;

import com.jvmu.jdi.types.SelfReferenceType;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.lang.reflect.Method;

/**
 * Self Module Reference - JDI-like ModuleReference for Java 9+ module inspection
 */
public class SelfModuleReference {
    
    private final SelfVirtualMachine vm;
    private final Object module;
    
    public SelfModuleReference(SelfVirtualMachine vm, Object module) {
        this.vm = vm;
        this.module = module;
    }
    
    /**
     * Get module name
     */
    public String name() {
        try {
            Method getNameMethod = module.getClass().getDeclaredMethod("getName");
            return (String) getNameMethod.invoke(module);
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    /**
     * Check if module is named
     */
    public boolean isNamed() {
        try {
            Method isNamedMethod = module.getClass().getDeclaredMethod("isNamed");
            return (Boolean) isNamedMethod.invoke(module);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if module is open
     */
    public boolean isOpen() {
        try {
            // Try to check if it's an open module
            String name = name();
            return name != null && name.contains("OPEN");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get module descriptor
     */
    public String descriptor() {
        try {
            Method getDescriptorMethod = module.getClass().getDeclaredMethod("getDescriptor");
            Object descriptor = getDescriptorMethod.invoke(module);
            return descriptor != null ? descriptor.toString() : "No descriptor";
        } catch (Exception e) {
            return "Descriptor unavailable";
        }
    }
    
    /**
     * Get module version
     */
    public String version() {
        try {
            Method getDescriptorMethod = module.getClass().getDeclaredMethod("getDescriptor");
            Object descriptor = getDescriptorMethod.invoke(module);
            
            if (descriptor != null) {
                Method versionMethod = descriptor.getClass().getDeclaredMethod("version");
                Object version = versionMethod.invoke(descriptor);
                return version != null ? version.toString() : "No version";
            }
        } catch (Exception e) {
            // Ignore
        }
        return "Version unavailable";
    }
    
    /**
     * Get packages in this module
     */
    public List<String> packages() {
        List<String> packages = new ArrayList<>();
        
        try {
            Method getPackagesMethod = module.getClass().getDeclaredMethod("getPackages");
            @SuppressWarnings("unchecked")
            Set<String> packageSet = (Set<String>) getPackagesMethod.invoke(module);
            packages.addAll(packageSet);
        } catch (Exception e) {
            // Fall back to analyzing classes
            List<Class<?>> allClasses = vm.getAllLoadedClasses();
            for (Class<?> clazz : allClasses) {
                try {
                    Method getModuleMethod = Class.class.getDeclaredMethod("getModule");
                    Object classModule = getModuleMethod.invoke(clazz);
                    
                    if (classModule == module) {
                        Package pkg = clazz.getPackage();
                        if (pkg != null && !packages.contains(pkg.getName())) {
                            packages.add(pkg.getName());
                        }
                    }
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }
        
        return packages;
    }
    
    /**
     * Get classes in this module
     */
    public List<SelfReferenceType> classes() {
        List<SelfReferenceType> classes = new ArrayList<>();
        
        List<Class<?>> allClasses = vm.getAllLoadedClasses();
        for (Class<?> clazz : allClasses) {
            try {
                Method getModuleMethod = Class.class.getDeclaredMethod("getModule");
                Object classModule = getModuleMethod.invoke(clazz);
                
                if (classModule == module) {
                    classes.add(new SelfReferenceType(vm, clazz));
                }
            } catch (Exception e) {
                // Ignore classes that can't be checked
            }
        }
        
        return classes;
    }
    
    /**
     * Get classes by package
     */
    public List<SelfReferenceType> classesByPackage(String packageName) {
        List<SelfReferenceType> result = new ArrayList<>();
        
        for (SelfReferenceType refType : classes()) {
            if (refType.name().startsWith(packageName + ".")) {
                result.add(refType);
            }
        }
        
        return result;
    }
    
    /**
     * Get class loader for this module
     */
    public SelfClassLoaderReference classLoader() {
        try {
            Method getClassLoaderMethod = module.getClass().getDeclaredMethod("getClassLoader");
            ClassLoader loader = (ClassLoader) getClassLoaderMethod.invoke(module);
            return loader != null ? new SelfClassLoaderReference(vm, loader) : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Check if package is exported
     */
    public boolean isExported(String packageName) {
        try {
            Method isExportedMethod = module.getClass().getDeclaredMethod("isExported", String.class);
            return (Boolean) isExportedMethod.invoke(module, packageName);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if package is exported to specific module
     */
    public boolean isExported(String packageName, SelfModuleReference target) {
        try {
            Method isExportedMethod = module.getClass().getDeclaredMethod("isExported", String.class, module.getClass());
            return (Boolean) isExportedMethod.invoke(module, packageName, target.module);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if package is open
     */
    public boolean isOpen(String packageName) {
        try {
            Method isOpenMethod = module.getClass().getDeclaredMethod("isOpen", String.class);
            return (Boolean) isOpenMethod.invoke(module, packageName);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if package is open to specific module
     */
    public boolean isOpen(String packageName, SelfModuleReference target) {
        try {
            Method isOpenMethod = module.getClass().getDeclaredMethod("isOpen", String.class, module.getClass());
            return (Boolean) isOpenMethod.invoke(module, packageName, target.module);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get module layer
     */
    public String layer() {
        try {
            Method getLayerMethod = module.getClass().getDeclaredMethod("getLayer");
            Object layer = getLayerMethod.invoke(module);
            return layer != null ? layer.toString() : "No layer";
        } catch (Exception e) {
            return "Layer unavailable";
        }
    }
    
    /**
     * Get the underlying Module object
     */
    public Object getModule() {
        return module;
    }
    
    /**
     * Get the virtual machine
     */
    public SelfVirtualMachine virtualMachine() {
        return vm;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SelfModuleReference)) return false;
        SelfModuleReference other = (SelfModuleReference) obj;
        return module.equals(other.module);
    }
    
    @Override
    public int hashCode() {
        return module.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("SelfModuleReference[name=%s, named=%b, classes=%d]",
            name(), isNamed(), classes().size());
    }
}