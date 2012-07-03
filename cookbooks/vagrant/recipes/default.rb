require_recipe "apt"

# Packages needed for development
%w{build-essential curl vim git-core}.each do | pkg |
  package pkg
end

# install java
script "install java" do
  interpreter "bash"
  user "root"
  cwd "/tmp"
  code <<-EOH
wget https://github.com/flexiondotorg/oab-java6/raw/0.2.4/oab-java.sh -O oab-java.sh
chmod +x oab-java.sh
./oab-java.sh
apt-get -y install sun-java6-jdk
  EOH
end

# increase the limit on open files
script "increase_open_file_limit" do
  interpreter "bash"
  user "root"
  cwd "/tmp"
  code <<-EOH
echo 'vagrant  soft  nofile  32000' >> /etc/security/limits.conf
echo 'vagrant  hard  nofile  64000' >> /etc/security/limits.conf
echo 'session required pam_limits.so' >> /etc/pam.d/common-session
  EOH
end

require_recipe "cassandra"
